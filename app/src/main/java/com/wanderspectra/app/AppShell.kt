package com.wanderspectra.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.wanderspectra.app.ui.theme.BackgroundCream
import com.wanderspectra.app.ui.theme.ButtonYellow
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.TanSongbird

enum class AppDestination {
    HOME,
    CHILD_PROFILE,
    HISTORY,
    COMMUNITY,
    SAFETY_CIRCLE
}

@Composable
fun AppShell(
    onSignOut: () -> Unit,
    content: @Composable () -> Unit
) {
    var showSettingsMenu by remember {
        mutableStateOf(false)
    }

    var currentDestination by remember {
        mutableStateOf(AppDestination.HOME)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCream)
        ) {

            // WanderSpectra Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 32.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Wander",
                    color = EmergencyRed,
                    fontFamily = TanSongbird,
                    fontSize = 22.sp
                )

                Text(
                    text = "Spectra",
                    color = PrimaryBlue,
                    fontFamily = TanSongbird,
                    fontSize = 22.sp
                )

                Image(
                    painter = painterResource(
                        id = R.drawable.wanderspectra_logo
                    ),
                    contentDescription = "Open Settings",
                    modifier = Modifier
                        .size(60.dp)
                        .clickable {
                            showSettingsMenu = !showSettingsMenu
                        }
                )
            }

            // Shared Content Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(
                        start = 28.dp,
                        end = 28.dp,
                        bottom = 8.dp
                    )
                    .background(
                        color = ButtonYellow,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {

                // Shared Inner Cream Card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = BackgroundCream,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = PrimaryBlue,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    when (currentDestination) {
                        AppDestination.HOME -> content()
                        AppDestination.CHILD_PROFILE -> ChildProfileContent()
                        AppDestination.HISTORY -> HistoryContent()
                        AppDestination.COMMUNITY -> CommunityContent()
                        AppDestination.SAFETY_CIRCLE -> SafetyCircleContent()
                    }
                }
            }

            AppBottomNavigation(
                onDestinationSelected = { destination ->
                    currentDestination = destination
                }
            )
        }

        if (showSettingsMenu) {
            SettingsMenu(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 105.dp,
                        end = 28.dp
                    ),
                onSignOut = onSignOut
            )
        }
    }
}

@Composable
fun AppBottomNavigation(
    onDestinationSelected: (AppDestination) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 28.dp,
                end = 28.dp,
                bottom = 18.dp
            )
            .background(
                color = BackgroundCream,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        AppNavigationButton(
            icon = Icons.Default.Home,
            contentDescription = "Home",
            onClick = {
                onDestinationSelected(AppDestination.HOME)
            }
        )

        AppNavigationButton(
            icon = Icons.Default.Person,
            contentDescription = "Child Profile",
            onClick = {
                onDestinationSelected(AppDestination.CHILD_PROFILE)
            }
        )

        AppNavigationButton(
            icon = Icons.Default.History,
            contentDescription = "History",
            onClick = {
                onDestinationSelected(AppDestination.HISTORY)
            }
        )

        AppNavigationButton(
            icon = Icons.Default.SupervisedUserCircle,
            contentDescription = "Safety Circle",
            onClick = {
                onDestinationSelected(AppDestination.SAFETY_CIRCLE)
            }
        )

        AppNavigationButton(
            icon = Icons.Default.Groups,
            contentDescription = "Community",
            onClick = {
                onDestinationSelected(AppDestination.COMMUNITY)
            }
        )
    }
}

@Composable
fun AppNavigationButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .background(
                color = ButtonYellow,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = PrimaryBlue,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = PrimaryBlue,
            modifier = Modifier.size(38.dp)
        )
    }
}

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit
) {
    Column(
        modifier = modifier
            .width(185.dp)
            .background(
                color = BackgroundCream,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = PrimaryBlue,
                shape = RoundedCornerShape(10.dp)
            )
    ) {

        Text(
            text = "Settings",
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = ButtonYellow,
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp
                    )
                )
                .padding(
                    horizontal = 18.dp,
                    vertical = 12.dp
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PrimaryBlue)
        )

        SettingsMenuItem("Caregiver Account")
        SettingsMenuItem("Safe Zone Settings")
        SettingsMenuItem("Wearable Settings")
        SettingsMenuItem("Alert & Notifications")
        SettingsMenuItem("Privacy & Permissions")
        SettingsMenuItem("Help & About")

        SettingsMenuItem(
            text = "Sign Out",
            onClick = {
                FirebaseAuth.getInstance().signOut()
                onSignOut()
            }
        )
    }
}

@Composable
fun SettingsMenuItem(
    text: String,
    onClick: () -> Unit = {}
) {
    Text(
        text = text,
        color = PrimaryBlue,
        fontFamily = Salsa,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 18.dp,
                vertical = 12.dp
            )
    )
}