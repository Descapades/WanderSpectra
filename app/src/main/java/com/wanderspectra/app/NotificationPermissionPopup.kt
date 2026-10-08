package com.wanderspectra.app

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.White

@Composable
fun NotificationPermissionPopup(
    activity: ComponentActivity
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(CaregiverNotificationManager.hasNotificationPermission(context))
    }

    var showRequestPopup by remember {
        mutableStateOf(!hasPermission)
    }

    var showDeniedPopup by remember {
        mutableStateOf(false)
    }


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentPermission = CaregiverNotificationManager.hasNotificationPermission(context)
                hasPermission = currentPermission
                if (currentPermission) {
                    showRequestPopup = false
                    showDeniedPopup = false
                }
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
        if (isGranted) {
            hasPermission = true
            showRequestPopup = false
            showDeniedPopup = false
            CaregiverNotificationManager.createNotificationChannel(context)
            CaregiverNotificationManager.sendCaregiverNotification(
                context,
                title = "Caregiver Notifications Active",
                message = "Notification permissions granted! WanderSpectra will alert you of child safety updates."
            )
        } else {
            showRequestPopup = false
            showDeniedPopup = true
        }
    }

    if (showRequestPopup && !hasPermission) {
        AlertDialog(
            onDismissRequest = {
                showRequestPopup = false
                showDeniedPopup = true
            },
            title = {
                Text(
                    text = "Notification Permission Request",
                    fontFamily = Salsa,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            },
            text = {
                Text(
                    text = "WanderSpectra requires notification permissions to alert caregivers about child safety and location updates. Would you like to enable notifications?",
                    fontFamily = Salsa,
                    color = SecondaryBlue
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            if (CaregiverNotificationManager.hasNotificationPermission(context)) {
                                hasPermission = true
                                showRequestPopup = false
                            } else {
                                CaregiverNotificationManager.openNotificationSettings(context)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "Allow",
                        fontFamily = Salsa,
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showRequestPopup = false
                        showDeniedPopup = true
                    }
                ) {
                    Text(
                        text = "Deny",
                        fontFamily = Salsa,
                        color = EmergencyRed
                    )
                }
            },
            containerColor = CardCream,
            shape = RoundedCornerShape(14.dp)
        )
    }

    if (showDeniedPopup && !hasPermission) {
        AlertDialog(
            onDismissRequest = {
                activity.finish()
            },
            title = {
                Text(
                    text = "Permission Denied",
                    fontFamily = Salsa,
                    fontWeight = FontWeight.Bold,
                    color = EmergencyRed
                )
            },
            text = {
                Text(
                    text = "Notification permissions required.",
                    fontFamily = Salsa,
                    color = SecondaryBlue
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        activity.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text(
                        text = "OK",
                        fontFamily = Salsa,
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = CardCream,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
