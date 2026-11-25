package com.wanderspectra.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val MapBlue = Color(0xFF4681B2)

private val ChildMarkerColors = listOf(
    Color(0xFFC94B5B),
    Color(0xFF4681B2),
    Color(0xFF39885B),
    Color(0xFFC69B23),
    Color(0xFF8B63B5),
    Color(0xFFDB803B)
)




@Composable
fun ChildLocationMap(
    children: List<ChildProfile>,
    focusedChildId: String? = null,
    focusRequest: Int = 0,
    onChildFocused: (String) -> Unit = {},
    reportedSightings: List<AlertSighting> = emptyList(),
    alertLocation: LatLng? = null
) {
    val context = LocalContext.current
    val wearableRepository = remember { WearableRepository() }
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
                        "ChildLocationMap",
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
            val childColor =
                ChildMarkerColors[index % ChildMarkerColors.size]

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
                        "ChildLocationMap",
                        "Unable to load marker photo for ${child.childId}",
                        e
                    )
                    null
                }
            } else {
                null
            }

            childMarkerIcons = childMarkerIcons + (
                    child.childId to createChildMarker(
                        photo = photo,
                        color = childColor
                    )
                    )
        }
    }

    // Request fresh locations from connected wearables.
    LaunchedEffect(children) {
        while (true) {
            children.forEach { child ->
                wearableRepository.getWearable(
                    childId = child.childId,
                    onSuccess = { wearable ->
                        val deviceId = wearable?.deviceId

                        if (!deviceId.isNullOrBlank()) {
                            WearConnectionManager.requestWearableLocation(
                                context = context,
                                nodeId = deviceId
                            ) { result ->

                                if (!result.connected) {
                                    wearableRepository.updateWearableConnection(
                                        childId = child.childId,
                                        connected = false,
                                        onSuccess = {
                                            android.util.Log.i(
                                                "ChildLocationMap",
                                                "Wearable disconnected: ${child.childId}"
                                            )
                                        },
                                        onFailure = { exception ->
                                            android.util.Log.e(
                                                "ChildLocationMap",
                                                "Failed to update wearable connection",
                                                exception
                                            )
                                        }
                                    )
                                }

                                if (
                                    result.connected &&
                                    wearable?.connected == false
                                ) {
                                    wearableRepository.updateWearableConnection(
                                        childId = child.childId,
                                        connected = true,
                                        onSuccess = {
                                            android.util.Log.i(
                                                "ChildLocationMap",
                                                "Wearable reconnected: ${child.childId}"
                                            )
                                        },
                                        onFailure = { exception ->
                                            android.util.Log.e(
                                                "ChildLocationMap",
                                                "Failed to restore wearable connection",
                                                exception
                                            )
                                        }
                                    )
                                }

                                if (
                                    result.connected &&
                                    result.batteryLevel in 0..100
                                ) {
                                    wearableRepository.updateWearableBattery(
                                        childId = child.childId,
                                        batteryLevel = result.batteryLevel,
                                        onSuccess = {
                                            android.util.Log.i(
                                                "HomeBattery",
                                                "Battery updated to ${result.batteryLevel}%"
                                            )
                                        },
                                        onFailure = { exception ->
                                            android.util.Log.e(
                                                "HomeBattery",
                                                "Failed to update wearable battery",
                                                exception
                                            )
                                        }
                                    )

                                    val currentBattery = result.batteryLevel

                                    if (currentBattery <= 20) {
                                        if (
                                            CaregiverNotificationPreferences
                                                .isLowBatteryAlertEnabled(context) &&
                                            !CaregiverNotificationPreferences
                                                .hasLowBatteryNotificationBeenSent(
                                                    context,
                                                    child.childId
                                                )
                                        ) {
                                            val notificationSent =
                                                CaregiverNotificationManager
                                                    .sendCaregiverNotification(
                                                        context = context,
                                                        title = "Wearable Low Battery",
                                                        message = "${child.fullName}'s wearable battery is at $currentBattery%. Please charge the device."
                                                    )

                                            if (notificationSent) {
                                                CaregiverNotificationPreferences
                                                    .setLowBatteryNotificationSent(
                                                        context,
                                                        child.childId,
                                                        true
                                                    )
                                            }
                                        }
                                    } else {
                                        CaregiverNotificationPreferences
                                            .setLowBatteryNotificationSent(
                                                context,
                                                child.childId,
                                                false
                                            )
                                    }
                                }

                                if (
                                    result.connected &&
                                    result.latitude != null &&
                                    result.longitude != null
                                ) {
                                    val latitude = result.latitude
                                    val longitude = result.longitude

                                    wearableRepository.updateWearableLocation(
                                        childId = child.childId,
                                        latitude = latitude,
                                        longitude = longitude,
                                        timestamp = result.timestamp,
                                        onSuccess = {
                                            childLocations = childLocations + (
                                                    child.childId to LatLng(
                                                        latitude,
                                                        longitude
                                                    )
                                                    )
                                        },
                                        onFailure = { exception ->
                                            android.util.Log.e(
                                                "ChildLocationMap",
                                                "Failed to save location for ${child.childId}",
                                                exception
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    },
                    onFailure = { exception ->
                        android.util.Log.e(
                            "ChildLocationMap",
                            "Failed to retrieve wearable for ${child.childId}",
                            exception
                        )
                    }
                )
            }

            delay(30_000L)
        }
    }

    val cameraPositionState = rememberCameraPositionState()



LaunchedEffect(
childLocations,
children,
reportedSightings,
alertLocation,
focusedChildId
) {

    if (focusedChildId != null) {
        return@LaunchedEffect
    }

    val locations = buildList {
        children.forEach { child ->
            childLocations[child.childId]?.let { add(it) }
        }
        alertLocation?.let { add(it) }

        reportedSightings.forEach { sighting ->
            add(
                LatLng(
                    sighting.latitude,
                    sighting.longitude
                )
            )
        }
    }


    if (locations.isEmpty()) {
            return@LaunchedEffect
        }

        if (locations.size == 1) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    locations.first(),
                    16f
                )
            )
        } else {
            val bounds = LatLngBounds.builder().apply {
                locations.forEach { include(it) }
            }.build()

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    80
                )
            )
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, MapBlue)
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
                            title = child.preferredName.ifBlank {
                                child.fullName
                            },
                            icon = childMarkerIcons[child.childId]
                                ?: BitmapDescriptorFactory.defaultMarker(
                                    floatArrayOf(
                                        0f, 210f, 120f,
                                        45f, 270f, 30f
                                    )[
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


            alertLocation?.let { location ->
                val markerState = rememberMarkerState(
                    key = "community_alert_location",
                    position = location
                )

                LaunchedEffect(location) {
                    markerState.position = location
                }

                Marker(
                    state = markerState,
                    title = "Child's Last Known Location",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED
                    )
                )
            }


            reportedSightings.forEach { sighting ->
                key(sighting.id) {
                    val sightingLocation = LatLng(
                        sighting.latitude,
                        sighting.longitude
                    )

                    val markerState = rememberMarkerState(
                        key = "sighting_${sighting.id}",
                        position = sightingLocation
                    )

                    Marker(
                        state = markerState,
                        title = "Reported Sighting (Unverified)",
                        snippet = "Direction: ${sighting.direction}",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                        )
                    )
                }
            }


            LaunchedEffect(focusedChildId, focusRequest) {
                val location = focusedChildId?.let {
                    childLocations[it]
                }

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


