package com.wanderspectra.app

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.SafeMint
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.White

@Composable
fun PrivacyPermissionsContent() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasNotificationPermission by remember {
        mutableStateOf(CaregiverNotificationManager.hasNotificationPermission(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = CaregiverNotificationManager.hasNotificationPermission(context)
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
        hasNotificationPermission = CaregiverNotificationManager.hasNotificationPermission(context)
        if (isGranted) {
            CaregiverNotificationManager.createNotificationChannel(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Privacy & Permissions",
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (hasNotificationPermission) SafeMint else PrimaryBlue,
                    shape = RoundedCornerShape(10.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = CardCream),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasNotificationPermission) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = "Notification Permission",
                            tint = if (hasNotificationPermission) SafeMint else EmergencyRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Caregiver Notifications",
                                color = PrimaryBlue,
                                fontFamily = Salsa,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasNotificationPermission) "Granted / Enabled" else "Denied / Disabled",
                                color = if (hasNotificationPermission) SafeMint else EmergencyRed,
                                fontFamily = Salsa,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Allows WanderSpectra to send immediate caregiver alerts for safe zone breaches, emergency contacts, and wearable battery status.",
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 12.sp
                )

                if (!hasNotificationPermission) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                CaregiverNotificationManager.openNotificationSettings(context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Request Permission",
                            color = White,
                            fontFamily = Salsa,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
