
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


class MainActivity : ComponentActivity() {
    private val callbackManager = CallbackManager.Factory.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        SightingReportNavigation.receive(intent)
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
                var isSavingChildren by remember { mutableStateOf(false) }
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
                        isSaving = isSavingChildren,
                        onBack = {
                            showChildConfirmation = false
                            showOnboarding = true
                        },
                        onFinish = {
                            if (confirmedChildren.isNotEmpty()) {
                                if (isSavingChildren) return@ChildConfirmation
                                isSavingChildren = true
                                var savedCount = 0
                                var hasFailed = false

                                val childrenToSave = confirmedChildren.map { child ->
                                    if (child.childId.isBlank()) {
                                        child.copy(
                                            childId = java.util.UUID.randomUUID().toString()
                                        )
                                    } else {
                                        child
                                    }
                                }
                                confirmedChildren = childrenToSave

                                childrenToSave.forEach { childProfile ->
                                    childProfileRepository.createChildProfile(
                                        childProfile = childProfile,
                                        onSuccess = {
                                            savedCount++

                                            if (savedCount == childrenToSave.size && !hasFailed) {
                                                isSavingChildren = false
                                                showChildConfirmation = false
                                                showHome = true
                                            }
                                        },
                                        onFailure = { exception ->
                                            hasFailed = true
                                            isSavingChildren = false
                                            exception.printStackTrace()
                                        }
                                    )
                                }
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




