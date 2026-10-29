package com.wanderspectra.app

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun ElopementActiveContent(
    incidentId: String?,
    statusText: String,
    pointLat: Double = 0.0,
    pointLng: Double = 0.0,
    childName: String = "",
    onChildSafe: (String) -> Unit,
    onBackHome: () -> Unit,
    isBusy: Boolean = false,
    caregiverId: String = "",
    childId: String = "",
    onEmergencyProfile: (() -> Unit)? = null,
    emergencyProfileAvailable: Boolean = false,
    onSafetyCircle: (() -> Unit)? = null,
    onCommunity: (() -> Unit)? = null
) {
    var showLocation by remember(incidentId) { mutableStateOf(false) }
    var isWatchConnected by remember(childId) { mutableStateOf(false) }
    var watchBatteryLevel by remember(childId) { mutableIntStateOf(-1) }
    var watchLastSync by remember(childId) { mutableLongStateOf(0L) }
    val wearableRepository = remember { WearableRepository() }

    LaunchedEffect(childId) {
        if (childId.isNotBlank()) {
            wearableRepository.getWearable(
                childId = childId,
                onSuccess = { wearable ->
                    isWatchConnected = wearable?.connected == true
                    watchBatteryLevel = wearable?.batteryLevel ?: -1
                    watchLastSync = wearable?.lastSync ?: 0L
                },
                onFailure = { exception ->
                    android.util.Log.e(
                        "ElopementActive",
                        "Failed to retrieve wearable for child",
                        exception
                    )
                }
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ELOPEMENT\nMODE ACTIVE",
            color = EmergencyRed,
            fontFamily = TanSongbird,
            fontSize = 20.sp,
            lineHeight = 40.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "$childName is missing!",
            color = SecondaryRed,
            fontFamily = Salsa,
            fontSize = 34.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (isWatchConnected) "● WATCH CONNECTED" else "● WATCH NOT CONNECTED",
            color = if (isWatchConnected) Color(0xFF86DDB2) else SecondaryRed,
            fontFamily = Salsa,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (watchBatteryLevel >= 0) {
                "Battery $watchBatteryLevel%"
            } else {
                "Battery Unknown"
            },
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append("Last Synced: ")
                }
                withStyle(SpanStyle(color = PrimaryBlue)) {
                    append(
                        if (watchLastSync > 0L) {
                            java.text.SimpleDateFormat(
                                "MMM d, h:mm a",
                                java.util.Locale.getDefault()
                            ).format(java.util.Date(watchLastSync))
                        } else {
                            "Unknown"
                        }
                    )
                }
            },
            fontFamily = Salsa,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )


        onSafetyCircle?.let { action ->

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notify Safety Circle",
                color = SecondaryBlue,
                fontFamily = Salsa,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Sends the active elopement alert and child profile " +
                        "to trusted Safety Circle members.",
                color = Color(0xFF478AC2),
                fontFamily = Salsa,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = action,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
                    .border(
                        width = 1.dp,
                        color = SecondaryBlue,
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBlue,
                    contentColor = PrimaryBlue
                )
            ) {
                Text(
                    text = "Notify Safety Circle",
                    fontFamily = Salsa,
                    fontSize = 18.sp,
                    color = SecondaryBlue
                )
            }
        }
        onCommunity?.let { action ->

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Issue Community Alert",
                color = SecondaryBlue,
                fontFamily = Salsa,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Share $childName's missing-child information " +
                        "with nearby participating WanderSpectra users.",
                color = Color(0xFF478AC2),
                fontFamily = Salsa,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = action,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
                    .border(
                        width = 1.dp,
                        color = SecondaryBlue,
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBlue,
                    contentColor = SecondaryBlue
                )
            ) {
                Text(
                    text = "Community Alert",
                    fontFamily = Salsa,
                    fontSize = 18.sp
                )
            }
        }
        EmergencyServicesShortcut()
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Child is Safe",
            color = Color(0xFF168452),
            fontFamily = Salsa,
            fontSize = 26.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ends Elopement Mode and completes the incident record.",
            color = Color(0xFF29AE6A),
            fontFamily = Salsa,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { incidentId?.let(onChildSafe) },
            enabled = incidentId != null && !isBusy,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFF168452),
                    shape = RoundedCornerShape(10.dp)
                ),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFBFE4C8),
                contentColor = Color(0xFF168452)
            )
        ) {
            Text(
                text = if (isBusy) "Saving..." else "Child is Safe",
                fontFamily = Salsa,
                fontSize = 18.sp
            )
        }
    }
}
