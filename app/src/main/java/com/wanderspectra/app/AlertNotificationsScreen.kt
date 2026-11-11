package com.wanderspectra.app

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonYellow
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.SafeMint
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.text.style.TextAlign
import com.wanderspectra.app.ui.theme.SecondaryRed
import com.wanderspectra.app.ui.theme.TanSongbird
import androidx.compose.ui.graphics.Color

@Composable
fun AlertNotificationsContent() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(CaregiverNotificationManager.hasNotificationPermission(context)) }
    var communityAlertsEnabled by remember { mutableStateOf(CommunityAlertPreferences.isOptedIn(context)) }
    val participantRepository = remember(context) { CommunityAlertParticipantRepository(context) }
    var communityAlertStatus by remember { mutableStateOf("") }
    var communityAlertRequestInProgress by remember { mutableStateOf(false) }
    var safeZoneExitEnabled by remember { mutableStateOf(false) }
    var wearableDisconnectedEnabled by remember { mutableStateOf(false) }
    var lowBatteryAlertEnabled by remember { mutableStateOf(false) }
    var safetyCircleUpdatesEnabled by remember { mutableStateOf(false) }
    var communitySightingsEnabled by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(false) }
    var vibrationEnabled by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = CaregiverNotificationManager.hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = CaregiverNotificationManager.hasNotificationPermission(context)
        if (isGranted) {
            CaregiverNotificationManager.createNotificationChannel(context)
            CaregiverNotificationManager.sendCaregiverNotification(
                context,
                title = "Caregiver Alerts Enabled",
                message = "Notifications are active for WanderSpectra caregiver safety updates."
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 0.dp
            )
            .background(
                color = CardCream,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = PrimaryBlue,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Alerts &\nNotifications",
            color = EmergencyRed,
            fontFamily = TanSongbird,
            fontSize = 20.sp,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardCream),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 4.dp,
                        end = 4.dp,
                        bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Safety Alerts",
                        color = PrimaryBlue,
                        fontFamily = TanSongbird,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Safe Zone Exit Alert",
                        color = SecondaryBlue,
                        fontFamily = Salsa,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = safeZoneExitEnabled,
                            onCheckedChange = { safeZoneExitEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue,
                                checkedBorderColor = PrimaryBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = EmergencyRed.copy(alpha = 0.35f),
                                uncheckedBorderColor = EmergencyRed
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Notify me when the child's wearable leaves an active safe zone",
                            color = PrimaryBlue,
                            fontFamily = Salsa,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Wearable Alerts",
                        color = PrimaryBlue,
                        fontFamily = TanSongbird,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Wearable Disconnected",
                        color = SecondaryBlue,
                        fontFamily = Salsa,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = wearableDisconnectedEnabled,
                            onCheckedChange = { wearableDisconnectedEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue,
                                checkedBorderColor = PrimaryBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = EmergencyRed.copy(alpha = 0.35f),
                                uncheckedBorderColor = EmergencyRed
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Notify me if the child's watch loses connection",
                            color = PrimaryBlue,
                            fontFamily = Salsa,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Low Battery Alert",
                        color = SecondaryBlue,
                        fontFamily = Salsa,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = lowBatteryAlertEnabled,
                            onCheckedChange = { lowBatteryAlertEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue,
                                checkedBorderColor = PrimaryBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = EmergencyRed.copy(alpha = 0.35f),
                                uncheckedBorderColor = EmergencyRed
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Notify me when the child's watch battery is low",
                            color = PrimaryBlue,
                            fontFamily = Salsa,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Elopement Notifications",
                        color = PrimaryBlue,
                        fontFamily = TanSongbird,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Safety Circle Updates",
                        color = SecondaryBlue,
                        fontFamily = Salsa,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = safetyCircleUpdatesEnabled,
                            onCheckedChange = { safetyCircleUpdatesEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue,
                                checkedBorderColor = PrimaryBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = EmergencyRed.copy(alpha = 0.35f),
                                uncheckedBorderColor = EmergencyRed
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Notify me when a safety circle member responds or provides information during an active elopement",
                            color = PrimaryBlue,
                            fontFamily = Salsa,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Community Sightings",
                        color = SecondaryBlue,
                        fontFamily = Salsa,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = communitySightingsEnabled,
                            onCheckedChange = { communitySightingsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue,
                                checkedBorderColor = PrimaryBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = EmergencyRed.copy(alpha = 0.35f),
                                uncheckedBorderColor = EmergencyRed
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Notify me when a sighting of the child is submitted during an active Community Alert.",
                            color = PrimaryBlue,
                            fontFamily = Salsa,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Community Alerts",
                        color = PrimaryBlue,
                        fontFamily = TanSongbird,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = communityAlertsEnabled,
                            enabled = !communityAlertRequestInProgress,
                            onCheckedChange = { enabled ->
                                // KEEP your existing Community Alerts
                                // registration and opt-out logic here
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue,
                                checkedBorderColor = PrimaryBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = EmergencyRed.copy(alpha = 0.35f),
                                uncheckedBorderColor = EmergencyRed
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Participate in nearby missing child alerts.",
                            color = PrimaryBlue,
                            fontFamily = Salsa,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Notification Preferences",
                        color = PrimaryBlue,
                        fontFamily = TanSongbird,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue,
                                checkedBorderColor = PrimaryBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = EmergencyRed.copy(alpha = 0.35f),
                                uncheckedBorderColor = EmergencyRed
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Sound",
                            color = PrimaryBlue,
                            fontFamily = Salsa,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue,
                                checkedBorderColor = PrimaryBlue,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = EmergencyRed.copy(alpha = 0.35f),
                                uncheckedBorderColor = EmergencyRed
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Vibration",
                            color = PrimaryBlue,
                            fontFamily = Salsa,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
