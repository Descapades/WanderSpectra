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
import android.content.Intent
import com.facebook.CallbackManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val callbackManager = CallbackManager.Factory.create()
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
                    mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                }

                var showAddAnotherChild by remember {
                    mutableStateOf(false)
                }

                var showChildConfirmation by remember {
                    mutableStateOf(false)
                }

                var confirmedChildren by remember {
                    mutableStateOf<List<ChildProfile>>(emptyList())
                }

                LaunchedEffect(Unit) {
                    delay(2000)
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreen()
                } else if (showHome) {
                    HomeScreen(
                        onSignOut = {
                            showHome = false
                            showOnboarding = false
                        }
                    )
                } else if (showAddAnotherChild) {

                    AddAnotherChild(
                        onChildProfileCreated = { childProfile ->

                            confirmedChildren =
                                confirmedChildren + childProfile

                            showAddAnotherChild = false
                            showChildConfirmation = true
                        }
                    )

                } else if (showChildConfirmation) {

                    ChildConfirmation(
                        children = confirmedChildren,

                        onAddAnotherChild = {
                            showChildConfirmation = false
                            showAddAnotherChild = true
                        },

                        onBack = {
                            showChildConfirmation = false
                            showOnboarding = true
                        },

                        onFinish = {
                            showChildConfirmation = false
                            showHome = true
                        }
                    )

                } else if (showOnboarding) {

                    OnboardingScreen(
                        onAccountCreated = { childProfile ->
                            confirmedChildren = listOf(childProfile)

                            showOnboarding = false
                            showChildConfirmation = true
                        }
                    )
                } else {
                    LoginScreen(
                        callbackManager = callbackManager,
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

    @Suppress("DEPRECATION")
    @Deprecated("Facebook SDK uses onActivityResult for login callbacks")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}



