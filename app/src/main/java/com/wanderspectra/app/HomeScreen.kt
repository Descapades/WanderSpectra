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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HomeBlue = Color(0xFF4681B2)
private val HomeRed = Color(0xFFC34A5B)
private val HomeMint = Color(0xFF8EDCB4)
private val HomeYellow = Color(0xFFFAE3A6)
private val HomeCream = Color(0xFFEEE9D5)
private val HomeButtonBlue = Color(0xFFA9C8D9)
private val HomeButtonRed = Color(0xFFE2A0A2)

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
        HomeContent(onChildMissing = {})
    }
}

@Composable
fun HomeContent(
    onChildMissing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HomeStatusCard()

        HomeLocationCard(onChildMissing = onChildMissing)
    }
}

@Composable
private fun HomeStatusCard() {

    val childRepository = remember { ChildProfileRepository() }
    val wearableRepository = remember { WearableRepository() }

    var childName by remember { mutableStateOf("Child") }
    var watchConnected by remember { mutableStateOf<Boolean?>(null) }
    var batteryLevel by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        childRepository.getChildProfiles(
            onSuccess = { children ->
                val child = children.firstOrNull()
                childName = child?.preferredName
                    ?.ifBlank { child.fullName }
                    ?: "Child"

                if (child != null) {
                    wearableRepository.getWearable(
                        childId = child.childId,
                        onSuccess = { wearable ->
                            watchConnected = wearable?.connected
                            batteryLevel = wearable?.batteryLevel
                                ?.takeIf { it >= 0 }
                        },
                        onFailure = {
                            watchConnected = null
                            batteryLevel = null
                        }
                    )
                }
            },
            onFailure = {
                watchConnected = null
                batteryLevel = null
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                color = HomeRed
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .background(
                            HomeRed,
                            RoundedCornerShape(50)
                        )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Safe Zone Unavailable",
                    fontFamily = Salsa,
                    fontSize = 18.sp,
                    color = HomeRed
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
        }
    }
}

@Composable
private fun HomeLocationCard(
    onChildMissing: () -> Unit
) {
    val context = LocalContext.current
    val childRepository = remember { ChildProfileRepository() }
    val wearableRepository = remember { WearableRepository() }

    var wearableLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationText by remember { mutableStateOf("Test Wearable Location") }
    var lastUpdatedText by remember { mutableStateOf("Test location") }
    var wearableDeviceId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        childRepository.getChildProfiles(
            onSuccess = { children ->
                val child = children.firstOrNull()
                if (child != null) {
                    wearableRepository.getWearable(
                        childId = child.childId,
                        onSuccess = { wearable ->
                            if (wearable != null) {
                                wearableDeviceId = wearable.deviceId
                                if (wearable.latitude != null && wearable.longitude != null) {
                                    wearableLocation = LatLng(wearable.latitude, wearable.longitude)
                                    locationText = "%.4f, %.4f".format(wearable.latitude, wearable.longitude)
                                }
                                val timestamp = if (wearable.locationTimestamp > 0L) {
                                    wearable.locationTimestamp
                                } else {
                                    wearable.lastSync
                                }
                                if (timestamp > 0L) {
                                    lastUpdatedText = formatLastUpdatedTime(timestamp)
                                }
                            }
                        },
                        onFailure = {}
                    )
                }
            },
            onFailure = {}
        )
    }

    val displayLocation = wearableLocation ?: LatLng(27.7731, -82.4076)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = Color(0xFFEDEBE2),
        border = BorderStroke(1.dp, HomeBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val cameraPositionState = rememberCameraPositionState()

            LaunchedEffect(displayLocation) {
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(
                        displayLocation,
                        16f
                    )
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(285.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, HomeBlue)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(
                            position = displayLocation
                        ),
                        title = "Wearable Location"
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Location:",
                    fontFamily = Salsa,
                    fontSize = 20.sp,
                    color = HomeRed
                )

                Text(
                    text = locationText,
                    modifier = Modifier.padding(start = 16.dp),
                    fontFamily = Salsa,
                    fontSize = 17.sp,
                    color = HomeBlue
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Last Updated:",
                    fontFamily = Salsa,
                    fontSize = 20.sp,
                    color = HomeRed
                )

                Text(
                    text = lastUpdatedText,
                    modifier = Modifier.padding(start = 16.dp),
                    fontFamily = Salsa,
                    fontSize = 17.sp,
                    color = HomeBlue
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    android.widget.Toast.makeText(context, "Playing sound on wearable...", android.widget.Toast.LENGTH_SHORT).show()
                    if (!wearableDeviceId.isNullOrBlank()) {
                        WearConnectionManager.playSoundOnWearable(context, wearableDeviceId!!) { success ->
                            if (!success) {
                                WearConnectionManager.getAvailableWearables(context) { nodes ->
                                    val node = nodes.firstOrNull()
                                    if (node != null) {
                                        WearConnectionManager.playSoundOnWearable(context, node.id) { _ -> }
                                    }
                                }
                            }
                        }
                    } else {
                        WearConnectionManager.getAvailableWearables(context) { nodes ->
                            val node = nodes.firstOrNull()
                            if (node != null) {
                                WearConnectionManager.playSoundOnWearable(context, node.id) { _ -> }
                            } else {
                                android.widget.Toast.makeText(context, "No connected wearable found", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(48.dp),
                shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomeButtonBlue,
                    contentColor = HomeBlue
                ),
                border = BorderStroke(1.dp, HomeBlue)
            ) {
                Text(
                    text = "Play Sound",
                    fontFamily = Salsa,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onChildMissing,
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(48.dp),
                shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomeButtonRed,
                    contentColor = HomeRed
                ),
                border = BorderStroke(1.dp, HomeRed)
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
