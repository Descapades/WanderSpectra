package com.wanderspectra.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wanderspectra.app.ui.theme.WanderSpectraTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WanderSpectraTheme {

                var showSplash by remember {
                    mutableStateOf(true)
                }

                var showOnboarding by remember {
                    mutableStateOf(false)
                }

                var showHome by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(Unit) {
                    delay(2000)
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreen()
                } else if (showHome) {
                    HomeScreen()
                } else if (showOnboarding) {
                    OnboardingScreen()
                } else {
                    LoginScreen(
                        onCreateAccountClick = {
                            showOnboarding = true
                        },
                        onLoginSuccess = {
                            showHome = true
                        }
                    )
                }
            }
        }
    }
}



