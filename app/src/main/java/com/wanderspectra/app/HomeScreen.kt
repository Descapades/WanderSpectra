package com.wanderspectra.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.wanderspectra.app.ui.theme.SafeMint
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import com.wanderspectra.app.ui.theme.TanSongbird
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HomeBlue = Color(0xFF4681B2)
private val HomeRed = Color(0xFFC34A5B)
private val HomeButtonBlue = Color(0xFFA9C8D9)
private val HomeButtonRed = Color(0xFFE2A0A2)

private val ChildMarkerColors = listOf(
    Color(0xFFC94B5B),
    Color(0xFF4681B2),
    Color(0xFF39885B),
    Color(0xFFC69B23),
    Color(0xFF8B63B5),
    Color(0xFFDB803B)
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
    AppShell(onSignOut = onSignOut)
}

@Composable
fun HomeContent(
    children: List<ChildProfile>,
    activeElopementChildId: String?,
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
        ChildLocationMap(
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
                    isActiveElopement = activeElopementChildId == child.childId,
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
    isActiveElopement: Boolean,
    onChildMissing: (ChildProfile) -> Unit,
    onChildSelected: () -> Unit
) {

    val wearableRepository = remember { WearableRepository() }
        val childMonitoringRepository = remember { ChildSafeZoneMonitoringRepository() }
        var childSafeZoneMonitoring by remember(child.childId) { mutableStateOf<ChildSafeZoneMonitoring?>(null) }
        val childName = child.preferredName.ifBlank { child.fullName }
        val context = LocalContext.current
        var wearableDeviceId by remember(child.childId) { mutableStateOf<String?>(null) }
        var watchConnected by remember { mutableStateOf<Boolean?>(null) }
        var previousWatchConnected by remember(child.childId) { mutableStateOf<Boolean?>(null) }
        var batteryLevel by remember { mutableStateOf<Int?>(null) }
        var locationText by remember(child.childId) { mutableStateOf("Location unavailable") }
        var lastUpdatedText by remember(child.childId) { mutableStateOf("Unknown") }
        var wearableLocation by remember(child.childId) { mutableStateOf<LatLng?>(null) }
        var isSoundPlaying by remember(child.childId) { mutableStateOf(false) }

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

            while (true) {
                wearableRepository.getWearable(
                    childId = child.childId,
                    onSuccess = { wearable ->
                        wearableDeviceId = wearable?.deviceId
                        val currentConnection = wearable?.connected

                        if (
                            previousWatchConnected == true &&
                            currentConnection == false &&
                            CaregiverNotificationPreferences.isWearableDisconnectedEnabled(context)
                        ) {
                            CaregiverNotificationManager.sendCaregiverNotification(
                                context = context,
                                title = "Wearable Disconnected",
                                message = "$childName's wearable has disconnected."
                            )
                        }

                        previousWatchConnected = currentConnection
                        watchConnected = currentConnection
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

                delay(5_000L)
            }
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
                    fontSize = 20.sp,
                    color = childColor
                )

                if (isActiveElopement) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "ACTIVE ELOPEMENT",
                        fontFamily = Salsa,
                        fontSize = 16.sp,
                        color = HomeRed
                    )
                }

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
                            if (isSoundPlaying) {
                                WearConnectionManager.stopSoundOnWearable(
                                    context,
                                    deviceId
                                ) { success ->
                                    if (success) {
                                        isSoundPlaying = false
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Unable to stop sound on this child's wearable",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                WearConnectionManager.playSoundOnWearable(
                                    context,
                                    deviceId
                                ) { success ->
                                    if (success) {
                                        isSoundPlaying = true
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Unable to play sound on this child's wearable",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
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
                        text = if (isSoundPlaying) "Stop Sound" else "Play Sound",
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
                        text = "ELOPEMENT MODE",
                        fontFamily = Salsa,
                        fontSize = 18.sp,
                        maxLines = 1
                    )
                }

            }
        }
    }

