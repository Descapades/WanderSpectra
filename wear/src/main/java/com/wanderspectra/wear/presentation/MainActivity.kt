package com.wanderspectra.wear.presentation

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.tooling.preview.devices.WearDevices
import com.wanderspectra.wear.R
import com.wanderspectra.wear.location.WearLocationManager
import com.wanderspectra.wear.presentation.theme.WanderSpectraTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import android.content.Intent
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.Text
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.wanderspectra.wear.presentation.theme.PrimaryBlue

private val BackgroundCream = Color(0xFFEEE9D5)

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val fineGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

        val coarseGranted =
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        Log.d(
            "MainActivity",
            "Location permissions granted: fine=$fineGranted, coarse=$coarseGranted"
        )

        if (fineGranted || coarseGranted) {
            startGeofenceMonitoringIfEnabled()
        } else {
            Log.w(
                "MainActivity",
                "Location permission denied. Geofence monitoring cannot start."
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        WearSafeZoneStorage.restore(this)
        requestLocationPermissionsIfNeeded()
        startGeofenceMonitoringIfEnabled()
        setContent {
            WearApp(activity = this)
        }
    }

    private fun requestLocationPermissionsIfNeeded() {
        if (!WearLocationManager.hasLocationPermission(this)) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun startGeofenceMonitoringIfEnabled() {
        val safeZone = WearSafetyStateRepository.activeSafeZone.value

        if (safeZone == null || !safeZone.monitoringEnabled) {
            Log.d("MainActivity", "No enabled Safe Zone. Monitoring not started.")
            return
        }

        if (!WearLocationManager.hasLocationPermission(this)) {
            Log.d("MainActivity", "Location permission missing. Monitoring not started.")
            return
        }

        val intent = Intent(
            this,
            WearGeofenceMonitoringService::class.java
        )

        try {
            ContextCompat.startForegroundService(this, intent)
            Log.d("MainActivity", "Starting Safe Zone monitoring service")
        } catch (e: Exception) {
            Log.e("MainActivity", "Unable to start Safe Zone monitoring", e)
        }
    }
}

@Composable
fun WearApp(activity: ComponentActivity) {
    val isPlayingSound by WearSafetyStateRepository.isPlayingSound.collectAsState()
    val isElopement by WearSafetyStateRepository.isElopementDetected.collectAsState()

    LaunchedEffect(isElopement) {
        Log.d("WearAppUI", "UI observed elopement state: $isElopement")
    }

    WanderSpectraTheme {
        WearLocationPermissionPopup(activity = activity)

        when {
            isElopement -> StopScreen()
            else -> SafeScreen()
        }
    }
}

@Composable
fun SafeScreen(childName: String = "") {
    val context = LocalContext.current

    val savedChildName = context.getSharedPreferences(
        "wanderspectra_child",
        Context.MODE_PRIVATE
    ).getString("childName", "") ?: ""

    val displayName = childName.ifBlank { savedChildName }
    val salsaTypeface = remember {
        ResourcesCompat.getFont(context, R.font.salsa)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.wear_safe_face),
            contentDescription = "Safe",
            modifier = Modifier
                .size(135.dp)
                .offset(y = (-25).dp)
        )

        val greeting = if (displayName.isNotBlank()) {
            "Hi, $displayName!"
        } else {
            "Hi!"
        }

        Canvas(
            modifier = Modifier
                .width(200.dp)
                .height(100.dp)
                .offset(y = 40.dp)
        ) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = PrimaryBlue.toArgb()
                textSize = 22.sp.toPx()
                typeface = salsaTypeface
                textAlign = Paint.Align.LEFT
            }

            val path = Path().apply {
                addArc(
                    size.width * 0.05f,
                    -size.height * 0.85f,
                    size.width * 0.95f,
                    size.height * 0.85f,
                    155f,
                    -130f
                )
            }

            val pathLength = android.graphics.PathMeasure(path, false).length
            val textWidth = paint.measureText(greeting)

            val horizontalOffset = (pathLength - textWidth) / 2f

            drawContext.canvas.nativeCanvas.drawTextOnPath(
                greeting,
                path,
                horizontalOffset,
                0f,
                paint
            )
        }
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true
)
@Composable
fun DefaultPreview() {
    val context = LocalContext.current
    WanderSpectraTheme {
        if (context is ComponentActivity) {
            WearLocationPermissionPopup(activity = context)
        }
        SafeScreen()
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true
)
@Composable
fun StopScreenDefaultPreview() {
    WanderSpectraTheme {
        StopScreen()
    }
}

@Composable
fun WanderSpectraSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEE9D5)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.wear_splash_logo
            ),
            contentDescription = "WanderSpectra",
            modifier = Modifier.width(165.dp)
        )
    }
}
