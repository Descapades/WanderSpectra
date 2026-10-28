package com.wanderspectra.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.wanderspectra.app.ui.theme.Salsa
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wanderspectra.app.ui.theme.TanSongbird
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.wanderspectra.app.ui.theme.SafeMint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import android.location.Geocoder
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.key
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.ui.graphics.toArgb
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import android.graphics.drawable.BitmapDrawable
import com.google.android.gms.maps.model.BitmapDescriptor
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableIntStateOf
import com.google.android.gms.maps.model.LatLngBounds

private val HomeBlue = Color(0xFF4681B2)
private val HomeRed = Color(0xFFC34A5B)
private val HomeMint = Color(0xFF8EDCB4)
private val HomeYellow = Color(0xFFFAE3A6)
private val HomeCream = Color(0xFFEEE9D5)
private val HomeButtonBlue = Color(0xFFA9C8D9)
private val HomeButtonRed = Color(0xFFE2A0A2)

private val ChildMarkerColors = listOf(
    Color(0xFFC94B5B), // Red
    Color(0xFF4681B2), // Blue
    Color(0xFF39885B), // Green
    Color(0xFFC69B23), // Yellow
    Color(0xFF8B63B5), // Purple
    Color(0xFFDB803B)  // Orange
)

fun formatLastUpdatedTime(timestamp: Long): String {
    if (timestamp <= 0L) return "Unknown"
    val now = System.currentTimeMillis()
    val diffMs = now - timestamp
    if (diffMs < 0L) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    val diffMinutes = diffMs / (1000 * 60)
    return when {
        diffMs < 60_000L -> "Just now"
        diffMinutes < 60L -> "$diffMinutes ${if (diffMinutes == 1L) "min" else "mins"} ago"
        else -> {
            val sdf = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@Composable
fun HomeScreen(
    onSignOut: () -> Unit
) {
    AppShell(onSignOut = onSignOut) {
    }
}

@Composable
fun HomeContent(
    children: List<ChildProfile>,
    onChildMissing: (ChildProfile) -> Unit
) {
    var focusedChildId by remember { mutableStateOf<String?>(null) }
    var focusRequest by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HomeLocationCard(
            children = children,
            focusedChildId = focusedChildId,
            focusRequest = focusRequest,
            onChildFocused = { childId ->
                focusedChildId = childId
                focusRequest++
            }
        )

        children.forEachIndexed { index, child ->
            key(child.childId) {
                HomeStatusCard(
                    child = child,
                    childColor = ChildMarkerColors[index % ChildMarkerColors.size],
                    onChildMissing = onChildMissing,
                    onChildSelected = {
                        focusedChildId = child.childId
                        focusRequest++
                    }
                )
            }
        }
    }
}

    @Composable
    private fun HomeStatusCard(
        child: ChildProfile,
        childColor: Color,
        onChildMissing: (ChildProfile) -> Unit,
        onChildSelected: () -> Unit
    ) {

        val childRepository = remember { ChildProfileRepository() }
        val wearableRepository = remember { WearableRepository() }
        val childMonitoringRepository = remember { ChildSafeZoneMonitoringRepository() }
        var childSafeZoneMonitoring by remember(child.childId) { mutableStateOf<ChildSafeZoneMonitoring?>(null) }
        val childName = child.preferredName.ifBlank { child.fullName }
        val context = LocalContext.current
        var wearableDeviceId by remember(child.childId) { mutableStateOf<String?>(null) }
        var watchConnected by remember { mutableStateOf<Boolean?>(null) }
        var batteryLevel by remember { mutableStateOf<Int?>(null) }
        var locationText by remember(child.childId) { mutableStateOf("Location unavailable") }
        var lastUpdatedText by remember(child.childId) { mutableStateOf("Unknown") }
        var wearableLocation by remember(child.childId) { mutableStateOf<LatLng?>(null) }

        LaunchedEffect(child.childId) {
            childSafeZoneMonitoring = null

            while (true) {
                childMonitoringRepository.get(child.childId) { settings ->
                    childSafeZoneMonitoring = settings
                }

                delay(5_000L)
            }
        }

        LaunchedEffect(wearableLocation) {
            val location = wearableLocation

            if (location == null) {
                locationText = "Location unavailable"
                return@LaunchedEffect
            }

            locationText = "Loading address..."

            locationText = try {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.getDefault())
                        .getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                        ?.firstOrNull()
                        ?.getAddressLine(0)
                } ?: "Address unavailable"
            } catch (e: Exception) {
                "Address unavailable"
            }
        }

        LaunchedEffect(child.childId) {
            watchConnected = null
            batteryLevel = null

            wearableRepository.getWearable(
                childId = child.childId,
                onSuccess = { wearable ->
                    wearableDeviceId = wearable?.deviceId
                    watchConnected = wearable?.connected
                    batteryLevel = wearable?.batteryLevel?.takeIf { it >= 0 }

                    wearableLocation = if (
                        wearable?.latitude != null &&
                        wearable.longitude != null
                    ) {
                        LatLng(wearable.latitude, wearable.longitude)
                    } else {
                        null
                    }

                    val timestamp = wearable?.locationTimestamp
                        ?.takeIf { it > 0L }
                        ?: wearable?.lastSync
                        ?: 0L

                    lastUpdatedText = formatLastUpdatedTime(timestamp)
                },
                onFailure = {
                    wearableDeviceId = null
                    watchConnected = null
                    batteryLevel = null
                }
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onChildSelected),
            shape = RoundedCornerShape(15.dp),
            color = Color(0xFFEDEBE2),
            border = BorderStroke(1.dp, HomeBlue)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = childName,
                    fontFamily = TanSongbird,
                    fontSize = 34.sp,
                    color = childColor
                )

                Spacer(modifier = Modifier.height(6.dp))
                val isSafeZoneOn =
                    childSafeZoneMonitoring?.monitoringEnabled == true &&
                            !childSafeZoneMonitoring?.activeSafeZoneId.isNullOrBlank()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .background(
                                color = if (isSafeZoneOn) SafeMint else HomeRed,
                                shape = RoundedCornerShape(50)
                            )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (isSafeZoneOn) {
                            "SAFE ZONE ON"
                        } else {
                            "SAFE ZONE OFF"
                        },
                        fontFamily = Salsa,
                        fontSize = 18.sp,
                        color = if (isSafeZoneOn) SafeMint else HomeRed
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (watchConnected) {
                            true -> "Watch Connected"
                            false -> "Watch Not Connected"
                            null -> "Watch Status Unavailable"
                        },
                        fontFamily = Salsa,
                        fontSize = 14.sp,
                        color = HomeBlue,
                        maxLines = 1
                    )

                    Text(
                        text = batteryLevel?.let { "Battery $it%" } ?: "Battery --",
                        fontFamily = Salsa,
                        fontSize = 14.sp,
                        color = HomeBlue,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Location:",
                    fontFamily = Salsa,
                    fontSize = 20.sp,
                    color = HomeRed
                )

                Text(
                    text = locationText,
                    fontFamily = Salsa,
                    fontSize = 17.sp,
                    color = HomeBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Last Updated:",
                    fontFamily = Salsa,
                    fontSize = 20.sp,
                    color = HomeRed
                )

                Text(
                    text = lastUpdatedText,
                    fontFamily = Salsa,
                    fontSize = 17.sp,
                    color = HomeBlue
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        val deviceId = wearableDeviceId

                        if (deviceId.isNullOrBlank()) {
                            android.widget.Toast.makeText(
                                context,
                                "No wearable assigned to ${child.preferredName.ifBlank { child.fullName }}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            WearConnectionManager.playSoundOnWearable(
                                context,
                                deviceId
                            ) { success ->
                                if (!success) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Unable to play sound on this child's wearable",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(9.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomeButtonBlue,
                        contentColor = SecondaryBlue
                    ),
                    border = BorderStroke(1.dp, SecondaryBlue)
                ) {
                    Text(
                        text = "Play Sound",
                        fontFamily = Salsa,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { onChildMissing(child) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(9.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomeButtonRed,
                        contentColor = SecondaryRed
                    ),
                    border = BorderStroke(1.dp, SecondaryRed)
                ) {
                    Text(
                        text = "CHILD IS MISSING",
                        fontFamily = Salsa,
                        fontSize = 18.sp,
                        maxLines = 1
                    )
                }

            }
        }
    }

    @Composable
    private fun HomeLocationCard(
        children: List<ChildProfile>,
        focusedChildId: String?,
        focusRequest: Int,
        onChildFocused: (String) -> Unit
    ) {
        val context = LocalContext.current
        val childRepository = remember { ChildProfileRepository() }
        val wearableRepository = remember { WearableRepository() }
        var wearableLocation by remember { mutableStateOf<LatLng?>(null) }
        var wearableDeviceId by remember { mutableStateOf<String?>(null) }
        var trackedChildId by remember { mutableStateOf<String?>(null) }
        var childLocations by remember { mutableStateOf<Map<String, LatLng>>(emptyMap()) }
        var childMarkerIcons by remember { mutableStateOf<Map<String, BitmapDescriptor>>(emptyMap()) }

        LaunchedEffect(children) {
            childLocations = emptyMap()

            children.forEach { child ->
                wearableRepository.getWearable(
                    childId = child.childId,
                    onSuccess = { wearable ->
                        val latitude = wearable?.latitude
                        val longitude = wearable?.longitude

                        if (latitude != null && longitude != null) {
                            childLocations = childLocations + (
                                    child.childId to LatLng(latitude, longitude)
                                    )
                        }
                    },
                    onFailure = { exception ->
                        android.util.Log.e(
                            "HomeLocationCard",
                            "Failed to load location for ${child.childId}",
                            exception
                        )
                    }
                )
            }
        }

        LaunchedEffect(children) {
            childMarkerIcons = emptyMap()

            children.forEachIndexed { index, child ->
                val childColor = ChildMarkerColors[index % ChildMarkerColors.size]

                val photo = if (child.photoUrl.isNotBlank()) {
                    try {
                        withContext(Dispatchers.IO) {
                            val request = ImageRequest.Builder(context)
                                .data(child.photoUrl)
                                .allowHardware(false)
                                .build()

                            val result = context.imageLoader.execute(request)

                            if (result is SuccessResult) {
                                val drawable = result.drawable

                                if (drawable is BitmapDrawable) {
                                    drawable.bitmap
                                } else {
                                    null
                                }
                            } else {
                                null
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "HomeLocationCard",
                            "Unable to load marker photo for ${child.childId}",
                            e
                        )
                        null
                    }
                } else {
                    null
                }

                val markerIcon = createChildMarker(
                    photo = photo,
                    color = childColor
                )

                childMarkerIcons = childMarkerIcons + (child.childId to markerIcon)
            }
        }

        LaunchedEffect(trackedChildId, wearableDeviceId) {
            val childId = trackedChildId ?: return@LaunchedEffect

            while (true) {
                WearConnectionManager.getAvailableWearables(context) { nodes ->

                    val node = nodes.firstOrNull {
                        it.id == wearableDeviceId &&
                                !wearableDeviceId.isNullOrBlank()
                    }

                    if (node != null) {
                        WearConnectionManager.requestWearableLocation(
                            context = context,
                            nodeId = node.id
                        ) { result ->

                            if (result.connected &&
                                result.latitude != null &&
                                result.longitude != null
                            ) {
                                val latitude = result.latitude
                                val longitude = result.longitude

                                wearableRepository.updateWearableLocation(
                                    childId = childId,
                                    latitude = latitude,
                                    longitude = longitude,
                                    timestamp = result.timestamp,
                                    onSuccess = {
                                        wearableLocation = LatLng(latitude, longitude)
                                    },
                                    onFailure = { exception ->
                                        android.util.Log.e(
                                            "HomeLocationCard",
                                            "Failed to save wearable location",
                                            exception
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                delay(30_000L)
            }
        }

        val displayLocation = children.firstNotNullOfOrNull { child ->
            childLocations[child.childId]
        }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val cameraPositionState = rememberCameraPositionState()

                var initialCameraPositionSet by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(childLocations, focusedChildId) {
                    if (initialCameraPositionSet || focusedChildId != null) {
                        return@LaunchedEffect
                    }

                    val locations = children.mapNotNull { child ->
                        childLocations[child.childId]
                    }

                    if (locations.isEmpty()) {
                        return@LaunchedEffect
                    }

                    if (locations.size == 1) {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(
                                locations.first(),
                                16f
                            )
                        )
                    } else {
                        val bounds = LatLngBounds.builder().apply {
                            locations.forEach { include(it) }
                        }.build()

                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngBounds(
                                bounds,
                                80
                            )
                        )
                    }

                    initialCameraPositionSet = true
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, HomeBlue)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        children.forEach { child ->
                            val location = childLocations[child.childId]

                            if (location != null) {
                                key(child.childId) {
                                    val markerState = rememberMarkerState(
                                        key = child.childId,
                                        position = location
                                    )

                                    LaunchedEffect(location) {
                                        markerState.position = location
                                    }

                                    Marker(
                                        state = markerState,
                                        title = child.preferredName.ifBlank { child.fullName },
                                        icon = childMarkerIcons[child.childId]
                                            ?: BitmapDescriptorFactory.defaultMarker(
                                            floatArrayOf(0f, 210f, 120f, 45f, 270f, 30f)[
                                                children.indexOf(child) % 6
                                            ]
                                        ),
                                        onClick = {
                                            onChildFocused(child.childId)
                                            true
                                        }
                                    )
                                }
                            }
                        }
                    }
                    LaunchedEffect(focusedChildId, focusRequest) {
                        val location = focusedChildId?.let { childLocations[it] }

                        if (location != null) {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    location,
                                    17f
                                ),
                                600
                            )
                        }
                    }
                }
            }
        }
    }
private fun createChildMarker(
    photo: Bitmap?,
    color: Color
): BitmapDescriptor {
    val size = 144
    val borderWidth = 9f

    val bitmap = Bitmap.createBitmap(
        size,
        size,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
        style = Paint.Style.FILL
    }

    canvas.drawCircle(
        size / 2f,
        size / 2f,
        size / 2f,
        borderPaint
    )

    val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }

    val radius = size / 2f - borderWidth

    canvas.save()

    val path = Path().apply {
        addCircle(
            size / 2f,
            size / 2f,
            radius,
            Path.Direction.CW
        )
    }

    canvas.clipPath(path)

    if (photo != null) {
        val scale = maxOf(
            (size - borderWidth * 2) / photo.width,
            (size - borderWidth * 2) / photo.height
        )

        val scaledWidth = photo.width * scale
        val scaledHeight = photo.height * scale

        val destination = RectF(
            (size - scaledWidth) / 2f,
            (size - scaledHeight) / 2f,
            (size + scaledWidth) / 2f,
            (size + scaledHeight) / 2f
        )

        canvas.drawBitmap(
            photo,
            null,
            destination,
            imagePaint
        )
    } else {
        canvas.drawColor(android.graphics.Color.WHITE)
    }

    canvas.restore()

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

