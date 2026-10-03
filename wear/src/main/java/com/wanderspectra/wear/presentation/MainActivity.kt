package com.wanderspectra.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.tooling.preview.devices.WearDevices
import com.wanderspectra.wear.R
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


private val BackgroundCream = Color(0xFFEEE9D5)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    WanderSpectraTheme {
        SafeScreen()
    }
}

@Composable
fun SafeScreen() {
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

        Image(
            painter = painterResource(id = R.drawable.wear_safe_text),
            contentDescription = "You are safe",
            modifier = Modifier
                .width(145.dp)
                .offset(y = 52.dp)
        )
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true
)
@Composable
fun DefaultPreview() {
    WearApp()
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