package com.wanderspectra.app

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun AlertNotificationsContent() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(CaregiverNotificationManager.hasNotificationPermission(context))
    }

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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Alert & Notifications",
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        // Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (hasPermission) SafeMint else EmergencyRed,
                    shape = RoundedCornerShape(10.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = CardCream),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hasPermission) Icons.Default.CheckCircle else Icons.Default.NotificationsOff,
                    contentDescription = if (hasPermission) "Granted" else "Denied",
                    tint = if (hasPermission) SafeMint else EmergencyRed,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (hasPermission) "Notifications Enabled" else "Notifications Disabled",
                        color = if (hasPermission) SecondaryBlue else EmergencyRed,
                        fontFamily = Salsa,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (hasPermission)
                            "Caregiver alerts are enabled and ready to deliver safety updates."
                        else
                            "Notification permission is denied. Notifications cannot be delivered.",
                        color = SecondaryBlue,
                        fontFamily = Salsa,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (!hasPermission) {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        CaregiverNotificationManager.openNotificationSettings(context)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Request Notification Permission",
                    color = com.wanderspectra.app.ui.theme.White,
                    fontFamily = Salsa,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Button(
            onClick = {
                val delivered = CaregiverNotificationManager.sendCaregiverNotification(
                    context,
                    title = "Caregiver Test Notification",
                    message = "This is a test alert for WanderSpectra caregiver notifications."
                )
                if (delivered) {
                    Toast.makeText(context, "Test notification sent!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Permission denied. Notification not sent.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasPermission) ButtonBlue else ButtonYellow
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Test Notification",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send Test Caregiver Notification",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
