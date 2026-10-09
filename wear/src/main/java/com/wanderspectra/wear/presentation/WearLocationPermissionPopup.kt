package com.wanderspectra.wear.presentation

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog

@Composable
fun WearLocationPermissionPopup(
    activity: ComponentActivity
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(WearLocationManager.hasLocationPermission(context))
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
                val currentPermission = WearLocationManager.hasLocationPermission(context)
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
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            hasPermission = true
            showRequestPopup = false
            showDeniedPopup = false
        } else {
            showRequestPopup = false
            showDeniedPopup = true
        }
    }

    if (showRequestPopup && !hasPermission) {
        Dialog(
            showDialog = showRequestPopup,
            onDismissRequest = {
                showRequestPopup = false
                showDeniedPopup = true
            }
        ) {
            Alert(
                title = { Text("Location Permission") },
                negativeButton = {
                    Button(
                        onClick = {
                            showRequestPopup = false
                            showDeniedPopup = true
                        },
                        colors = ButtonDefaults.secondaryButtonColors()
                    ) {
                        Text("Deny")
                    }
                },
                positiveButton = {
                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        colors = ButtonDefaults.primaryButtonColors()
                    ) {
                        Text("Allow")
                    }
                }
            ) {
                Text("WanderSpectra requires location permissions.")
            }
        }
    }

    if (showDeniedPopup && !hasPermission) {
        Dialog(
            showDialog = showDeniedPopup,
            onDismissRequest = {
                activity.finish()
            }
        ) {
            Alert(
                title = { Text("Permission Denied") },
                negativeButton = {},
                positiveButton = {
                    Button(
                        onClick = {
                            activity.finish()
                        },
                        colors = ButtonDefaults.primaryButtonColors()
                    ) {
                        Text("OK")
                    }
                }
            ) {
                Text("Location permissions required to use the app.")
            }
        }
    }
}
