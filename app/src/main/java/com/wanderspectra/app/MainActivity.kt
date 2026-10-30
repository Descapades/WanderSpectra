
package com.wanderspectra.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    private val callbackManager = CallbackManager.Factory.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        SightingReportNavigation.receive(intent)

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                val user = FirebaseAuth.getInstance().currentUser

                if (user == null) {
                    return@addOnSuccessListener
                }

                FirebaseFirestore.getInstance()
                    .collection("communityAlertParticipants")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        val savedToken = document.getString("fcmToken")
                    }
                    .addOnFailureListener {
                        Log.e("FCMTest", "Unable to read Firestore token")
                    }
            }
            .addOnFailureListener {
                Log.e("FCMTest", "Unable to retrieve FCM token")
            }

        enableEdgeToEdge()
        setContent {
            WanderSpectraTheme {
                NotificationPermissionPopup(activity = this@MainActivity)
                LocationPermissionPopup(activity = this@MainActivity)

                var showSplash by remember { mutableStateOf(true) }
                var showOnboarding by remember { mutableStateOf(false) }
                var showHome by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
                var showAddAnotherChild by remember { mutableStateOf(false) }
                var showChildConfirmation by remember { mutableStateOf(false) }
                var confirmedChildren by remember { mutableStateOf<List<ChildProfile>>(emptyList()) }
                val childProfileRepository = remember { ChildProfileRepository() }

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
                            val childProfile = confirmedChildren.firstOrNull()

                            if (childProfile != null) {
                                childProfileRepository.createChildProfile(
                                    childProfile = childProfile,
                                    onSuccess = { childId ->
                                        childProfileRepository.getChildProfile(
                                            childId = childId,
                                            onSuccess = { savedChild ->
                                                if (savedChild != null) {
                                                    showChildConfirmation = false
                                                    showHome = true
                                                }
                                            },
                                            onFailure = { exception ->
                                                exception.printStackTrace()
                                            }
                                        )
                                    },
                                    onFailure = { exception ->
                                        exception.printStackTrace()
                                    }
                                )
                            }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        SightingReportNavigation.receive(intent)
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




