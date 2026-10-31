package com.wanderspectra.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.SecondaryRed
import kotlinx.coroutines.launch

enum class AppDestination {
    HOME,
    CHILD_PROFILE,
    ADD_CHILD_PROFILE,
    HISTORY,
    SAFETY_CIRCLE,
    COMMUNITY,
    CAREGIVER_ACCOUNT,
    SAFE_ZONE_SETTINGS,
    SAFE_ZONE_EDIT,
    WEARABLE_SETTINGS,
    ALERT_NOTIFICATIONS,
    PRIVACY_PERMISSIONS,
    HELP_ABOUT,
    CHILD_MISSING,
    ELOPEMENT_MODE,
    EMERGENCY_PROFILE
}

@Composable
fun AppShell(
    onSignOut: () -> Unit
) {
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showChildProfileMenu by remember { mutableStateOf(false) }
    var showIncidentHistoryMenu by remember { mutableStateOf(false) }
    var showWearableSettingsMenu by remember { mutableStateOf(false) }
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var editingSafeZoneId by remember { mutableStateOf<String?>(null) }
    var openIncidentId by remember { mutableStateOf<String?>(null) }
    var incidentStatus by remember { mutableStateOf("") }
    var selectedIncidentReport by remember { mutableStateOf<IncidentRecord?>(null) }
    val context = LocalContext.current
    val caregiverUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val emergencyStore = remember(context, caregiverUid) {
        context.getSharedPreferences("elopement_state_" + caregiverUid, android.content.Context.MODE_PRIVATE)
    }
    var emergencyChildName by remember { mutableStateOf("") }
    var emergencyChildId by remember { mutableStateOf("") }
    var incidentBusy by remember { mutableStateOf(false) }
    LaunchedEffect(caregiverUid) {
        openIncidentId = emergencyStore.getString("incidentId", null)
        emergencyChildName = emergencyStore.getString("childName", "").orEmpty()
        emergencyChildId = emergencyStore.getString("childId", "").orEmpty()
        if (openIncidentId != null) {
            incidentStatus = "Active incident restored"
            currentDestination = AppDestination.ELOPEMENT_MODE
        }
    }
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) { }
    var emergencyProfileReturn by remember { mutableStateOf(AppDestination.CHILD_PROFILE) }
    var childProfiles by remember { mutableStateOf<List<ChildProfile>>(emptyList()) }
    var child by remember { mutableStateOf<ChildProfile?>(null) }
    val childRepository = remember { ChildProfileRepository() }
    val communityAlertRepository = remember { CommunityAlertRepository() }
    val wearableRepository = remember { WearableRepository() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        childRepository.getChildProfiles(
            onSuccess = { children ->
                childProfiles = children
                child = children.firstOrNull()
            },
            onFailure = {
                childProfiles = emptyList()
                child = null
            }
        )
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Wander", color = EmergencyRed, fontFamily = TanSongbird, fontSize = 22.sp)
                Text(text = "Spectra", color = PrimaryBlue, fontFamily = TanSongbird, fontSize = 22.sp)
                Image(
                    painter = painterResource(id = R.drawable.wanderspectra_logo),
                    contentDescription = "Open Settings",
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { showSettingsMenu = !showSettingsMenu }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 28.dp, bottom = 8.dp)
                    .background(
                        color = if (
                            currentDestination == AppDestination.CHILD_MISSING ||
                            currentDestination == AppDestination.ELOPEMENT_MODE
                        ) ButtonRed else ButtonYellow,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (
                            currentDestination == AppDestination.CHILD_MISSING ||
                            currentDestination == AppDestination.ELOPEMENT_MODE
                        ) SecondaryRed else PrimaryBlue,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = BackgroundCream, shape = RoundedCornerShape(12.dp))
                        .border(width = 1.dp, color = PrimaryBlue, shape = RoundedCornerShape(12.dp))
                ) {
                    when (currentDestination) {
                        AppDestination.HOME -> HomeContent(
                            children = childProfiles,
                            onChildMissing = { selectedChild ->
                                child = selectedChild
                                currentDestination = if (openIncidentId != null) {
                                    AppDestination.ELOPEMENT_MODE
                                } else {
                                    AppDestination.CHILD_MISSING
                                }
                            }
                        )
                        AppDestination.CHILD_PROFILE -> Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ChildProfileContent(
                                    selectedChild = child,
                                    onProfileUpdated = { updatedChild ->
                                        child = updatedChild

                                        childProfiles = childProfiles.map { profile ->
                                            if (profile.childId == updatedChild.childId) {
                                                updatedChild
                                            } else {
                                                profile
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        AppDestination.ADD_CHILD_PROFILE -> {
                            EditChildProfile(
                                child = ChildProfile(),
                                onCancel = {
                                    currentDestination = AppDestination.HOME
                                },
                                onSave = { newChild ->
                                    childRepository.createChildProfile(
                                        childProfile = newChild,
                                        onSuccess = { newChildId ->
                                            val savedChild = newChild.copy(
                                                childId = newChildId,
                                                caregiverUid = caregiverUid
                                            )

                                            childProfiles = childProfiles + savedChild
                                            child = savedChild
                                            currentDestination = AppDestination.CHILD_PROFILE
                                        },
                                        onFailure = { exception ->
                                            android.widget.Toast.makeText(
                                                context,
                                                exception.message ?: "Unable to save child profile.",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )
                                }
                            )
                        }
                        AppDestination.EMERGENCY_PROFILE -> EmergencyProfileContent(
                            profile = if (emergencyProfileReturn == AppDestination.ELOPEMENT_MODE)
                                childProfiles.firstOrNull { it.childId == emergencyChildId } else child,
                            onBack = {
                                currentDestination = emergencyProfileReturn
                            }
                        )
                        AppDestination.HISTORY -> IncidentHistoryScreen(
                            childId = if (openIncidentId != null) emergencyChildId else child?.childId.orEmpty(),
                            childName = if (openIncidentId != null) emergencyChildName else child?.preferredName.orEmpty(),
                            onViewReport = { incident ->
                                selectedIncidentReport = incident
                            }
                        )
                        AppDestination.SAFETY_CIRCLE -> SafetyCircleManagementScreen(
                            caregiverId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
                            childId = if (openIncidentId != null) emergencyChildId else child?.childId.orEmpty()
                        )
                        AppDestination.COMMUNITY -> CommunityContent()
                        AppDestination.CAREGIVER_ACCOUNT -> CaregiverAccountContent()
                        AppDestination.SAFE_ZONE_SETTINGS -> SafeZoneScreen(
                            onAddClick = {
                                editingSafeZoneId = null
                                currentDestination = AppDestination.SAFE_ZONE_EDIT
                            },
                            onEditClick = { zoneId ->
                                editingSafeZoneId = zoneId
                                currentDestination = AppDestination.SAFE_ZONE_EDIT
                            }
                        )

                        AppDestination.SAFE_ZONE_EDIT -> {
                            child?.let { childProfile ->
                                SafeZoneSettingsContent(
                                    childId = childProfile.childId,
                                    editingZoneId = editingSafeZoneId,
                                    onSaveSuccess = {
                                        editingSafeZoneId = null
                                        currentDestination = AppDestination.SAFE_ZONE_SETTINGS
                                    }
                                )
                            }
                        }

                        AppDestination.WEARABLE_SETTINGS -> {
                                WearableSettingsContent(
                                    children = childProfiles
                                )
                            }
                        AppDestination.ALERT_NOTIFICATIONS -> AlertNotificationsContent()
                        AppDestination.PRIVACY_PERMISSIONS -> PrivacyPermissionsContent()
                        AppDestination.HELP_ABOUT -> HelpAboutContent()
                        AppDestination.CHILD_MISSING -> ElopementConfirmationContent(
                            childName = child?.preferredName?.ifBlank { child?.fullName.orEmpty() }.orEmpty(),
                            statusText = incidentStatus,
                            isBusy = incidentBusy,
                            onConfirm = {
                                val selectedChild = child
                                when {
                                    incidentBusy -> Unit
                                    openIncidentId != null -> currentDestination = AppDestination.ELOPEMENT_MODE
                                    caregiverUid.isBlank() -> incidentStatus = "Sign in before activating Elopement Mode."
                                    selectedChild == null || selectedChild.childId.isBlank() ->
                                        incidentStatus = "Load a child profile before activating Elopement Mode."
                                    else -> {
                                        incidentBusy = true
                                        incidentStatus = "Saving incident..."
                                        ElopementWorkflow().startConfirmedIncident(
                                            childId = selectedChild.childId,
                                            caregiverId = caregiverUid
                                        ) { ok, message, id ->
                                            incidentBusy = false
                                            incidentStatus = message
                                            if (ok && !id.isNullOrBlank()) {
                                                openIncidentId = id
                                                emergencyChildId = selectedChild.childId
                                                emergencyChildName = selectedChild.preferredName.ifBlank { selectedChild.fullName }
                                                emergencyStore.edit()
                                                    .putString("incidentId", id)
                                                    .putString("childId", emergencyChildId)
                                                    .putString("childName", emergencyChildName)
                                                    .apply()
                                                currentDestination = AppDestination.ELOPEMENT_MODE
                                                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                                                    context, Manifest.permission.POST_NOTIFICATIONS
                                                ) != PackageManager.PERMISSION_GRANTED) {
                                                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                }
                                                ElopementNotifier.show(context, emergencyChildName)
                                            }
                                        }
                                    }
                                }
                            },
                            onCancel = {
                                if (!incidentBusy) {
                                    incidentStatus = ""
                                    currentDestination = AppDestination.HOME
                                }
                            }
                        )
                        AppDestination.ELOPEMENT_MODE -> ElopementActiveContent(
                            incidentId = openIncidentId,
                            statusText = incidentStatus,
                            childName = emergencyChildName,
                            caregiverId = caregiverUid,
                            childId = emergencyChildId,
                            onEmergencyProfile = {
                                emergencyProfileReturn = AppDestination.ELOPEMENT_MODE
                                currentDestination = AppDestination.EMERGENCY_PROFILE
                            },
                            emergencyProfileAvailable = childProfiles.any { it.childId == emergencyChildId },
                            onSafetyCircle = { currentDestination = AppDestination.SAFETY_CIRCLE },
                            onCommunity = {
                                val incidentId = openIncidentId

                                if (incidentId.isNullOrBlank()) {
                                    incidentStatus = "No active incident found."
                                } else {
                                    IncidentRepository().getIncidentsForChild(
                                        childId = emergencyChildId,
                                        onSuccess = { incidents ->
                                            val activeIncident = incidents.firstOrNull {
                                                it.incidentId == incidentId &&
                                                        it.status == "open"
                                            }

                                            if (activeIncident == null) {
                                                incidentStatus = "Active incident could not be found."
                                            } else {
                                                val latitude = activeIncident.lastKnownLatitude
                                                val longitude = activeIncident.lastKnownLongitude

                                                if (
                                                    latitude == null ||
                                                    longitude == null ||
                                                    !latitude.isFinite() ||
                                                    !longitude.isFinite() ||
                                                    latitude !in -90.0..90.0 ||
                                                    longitude !in -180.0..180.0 ||
                                                    (latitude == 0.0 && longitude == 0.0)
                                                ) {
                                                    incidentStatus =
                                                        "Last-known wearable location unavailable. Community Alert cannot be published."
                                                } else {
                                                    fun publishCommunityAlert(
                                                        selectedLatitude: Double,
                                                        selectedLongitude: Double,
                                                        selectedTimestamp: Long
                                                    ) {
                                                        coroutineScope.launch {
                                                            incidentStatus = "Creating Community Alert..."

                                                            try {
                                                                communityAlertRepository.createAlert(
                                                                    CommunityAlert(
                                                                        incidentId = incidentId,
                                                                        caregiverId = caregiverUid,
                                                                        childId = emergencyChildId,
                                                                        childName = emergencyChildName,
                                                                        latitude = selectedLatitude,
                                                                        longitude = selectedLongitude,
                                                                        locationTimestamp = selectedTimestamp,
                                                                        alertRadiusKm = 1.0
                                                                    )
                                                                )

                                                                incidentStatus = "Community Alert published successfully."
                                                                currentDestination = AppDestination.COMMUNITY

                                                            } catch (error: Exception) {
                                                                android.util.Log.e(
                                                                    "CommunityAlertDebug",
                                                                    "Community Alert creation failed",
                                                                    error
                                                                )

                                                                incidentStatus =
                                                                    error.message ?: "Unable to publish Community Alert."
                                                            }
                                                        }
                                                    }

                                                    wearableRepository.getWearable(
                                                        childId = emergencyChildId,
                                                        onSuccess = { wearable ->
                                                            val wearableLat = wearable?.latitude
                                                            val wearableLng = wearable?.longitude
                                                            val wearableTimestamp = wearable?.locationTimestamp ?: 0L

                                                            val validWearableLocation =
                                                                wearableLat != null &&
                                                                        wearableLng != null &&
                                                                        wearableLat.isFinite() &&
                                                                        wearableLng.isFinite() &&
                                                                        wearableLat in -90.0..90.0 &&
                                                                        wearableLng in -180.0..180.0 &&
                                                                        !(wearableLat == 0.0 && wearableLng == 0.0) &&
                                                                        wearableTimestamp > 0L &&
                                                                        wearableTimestamp <= System.currentTimeMillis()

                                                            if (wearableTimestamp > 0L) {
                                                                val locationAgeSeconds =
                                                                    (System.currentTimeMillis() - wearableTimestamp) / 1000L
                                                            }
                                                            if (validWearableLocation) {
                                                                publishCommunityAlert(
                                                                    wearableLat!!,
                                                                    wearableLng!!,
                                                                    wearableTimestamp
                                                                )
                                                            } else {
                                                                publishCommunityAlert(
                                                                    latitude,
                                                                    longitude,
                                                                    0L
                                                                )
                                                            }
                                                        },
                                                        onFailure = { error ->
                                                            android.util.Log.e(
                                                                "CommunityAlertDebug",
                                                                "Unable to retrieve wearable data; using incident location",
                                                                error
                                                            )

                                                            publishCommunityAlert(
                                                                latitude,
                                                                longitude,
                                                                0L
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        },
                                        onFailure = { error ->
                                            incidentStatus =
                                                error.message ?: "Unable to retrieve active incident."
                                        }
                                    )
                                }
                            },
                            isBusy = incidentBusy,
                            onChildSafe = { id ->
                                if (!incidentBusy && id == openIncidentId) {
                                    incidentBusy = true
                                    IncidentRepository().closeIncident(id) { ok, message ->
                                        incidentBusy = false
                                        incidentStatus = message
                                        if (ok) {
                                            coroutineScope.launch {
                                                try {
                                                    communityAlertRepository.resolveAlert(id)
                                                } catch (error: Exception) {
                                                    android.util.Log.e(
                                                        "CommunityAlert",
                                                        "Unable to resolve Community Alert",
                                                        error
                                                    )
                                                }

                                                emergencyStore.edit().clear().apply()
                                                openIncidentId = null
                                                emergencyChildId = ""
                                                emergencyChildName = ""
                                                currentDestination = AppDestination.HOME
                                            }
                                        }
                                    }
                                }
                            },
                            onBackHome = { currentDestination = AppDestination.HOME }
                        )
                    }
                }
            }

            if (openIncidentId != null && currentDestination != AppDestination.ELOPEMENT_MODE) {
                androidx.compose.material3.TextButton(onClick = {
                    currentDestination = AppDestination.ELOPEMENT_MODE
                }) { Text("Return to active Elopement Mode") }
            }

            CaregiverSightingNotifications(
                caregiverId = caregiverUid,
                childId = child?.childId.orEmpty()
            )

            AppBottomNavigation(
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    currentDestination = destination
                    showChildProfileMenu = false
                    showIncidentHistoryMenu = false
                },
                onChildProfileClick = {
                    showChildProfileMenu = !showChildProfileMenu
                    showIncidentHistoryMenu = false
                },
                onIncidentHistoryClick = {
                    showIncidentHistoryMenu = !showIncidentHistoryMenu
                    showChildProfileMenu = false
                }
            )
        }

        selectedIncidentReport?.let { report ->
            IncidentReportDialog(report = report, onDismiss = { selectedIncidentReport = null })
        }

        if (showChildProfileMenu) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, bottom = 105.dp)
                    .width(200.dp)
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
                childProfiles.forEach { profile ->
                    Text(
                        text = profile.preferredName.ifBlank { profile.fullName },
                        color = PrimaryBlue,
                        fontFamily = Salsa,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                child = profile
                                currentDestination = AppDestination.CHILD_PROFILE
                                showChildProfileMenu = false
                            }
                            .padding(12.dp)
                    )
                }
                SettingsMenuItem("+ Add Child Profile") {
                    showChildProfileMenu = false
                    currentDestination = AppDestination.ADD_CHILD_PROFILE
                }
            }
        }
        if (showIncidentHistoryMenu) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, bottom = 105.dp)
                    .width(200.dp)
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
                childProfiles.forEach { profile ->
                    Text(
                        text = profile.preferredName.ifBlank { profile.fullName },
                        color = PrimaryBlue,
                        fontFamily = Salsa,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                child = profile
                                openIncidentId = null
                                currentDestination = AppDestination.HISTORY
                                showIncidentHistoryMenu = false
                            }
                            .padding(12.dp)
                    )
                }
            }
        }

        if (showSettingsMenu) {
            SettingsMenu(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 105.dp, end = 28.dp),
                onSignOut = onSignOut,
                onDestinationSelected = { destination ->
                    currentDestination = destination
                    showSettingsMenu = false
                }
            )
        }
    }
}

@Composable
fun AppBottomNavigation(
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    onChildProfileClick: () -> Unit,
    onIncidentHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 28.dp, bottom = 18.dp)
            .background(color = BackgroundCream, shape = RoundedCornerShape(10.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        AppNavigationButton(
            icon = Icons.Default.Home,
            contentDescription = "Home",
            isSelected = currentDestination == AppDestination.HOME,
            onClick = {
                onDestinationSelected(AppDestination.HOME)
            }
        )

        AppNavigationButton(
            icon = Icons.Default.Person,
            contentDescription = "Child Profile",
            isSelected = currentDestination == AppDestination.CHILD_PROFILE,
            onClick = onChildProfileClick
        )

        AppNavigationButton(
            icon = Icons.Default.History,
            contentDescription = "History",
            isSelected = currentDestination == AppDestination.HISTORY,
            onClick = onIncidentHistoryClick
        )

        AppNavigationButton(
            icon = Icons.Default.SupervisedUserCircle,
            contentDescription = "Safety Circle",
            isSelected = currentDestination == AppDestination.SAFETY_CIRCLE,
            onClick = {
                onDestinationSelected(AppDestination.SAFETY_CIRCLE)
            }
        )

        AppNavigationButton(
            icon = Icons.Default.Groups,
            contentDescription = "Community",
            isSelected = currentDestination == AppDestination.COMMUNITY,
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
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) ButtonBlue else BackgroundCream,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(6.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
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
                ),
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
}

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit,
    onDestinationSelected: (AppDestination) -> Unit
) {
    Column(
        modifier = modifier
            .width(185.dp)
            .background(color = BackgroundCream, shape = RoundedCornerShape(10.dp))
            .border(width = 1.dp, color = PrimaryBlue, shape = RoundedCornerShape(10.dp))
    ) {
        Text(
            text = "Settings",
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = ButtonYellow, shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp)
        )
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PrimaryBlue))
        SettingsMenuItem("Caregiver Account") { onDestinationSelected(AppDestination.CAREGIVER_ACCOUNT) }
        SettingsMenuItem("Safe Zone Settings") { onDestinationSelected(AppDestination.SAFE_ZONE_SETTINGS) }
        SettingsMenuItem("Wearable Settings") { onDestinationSelected(AppDestination.WEARABLE_SETTINGS) }
        SettingsMenuItem("Alert & Notifications") { onDestinationSelected(AppDestination.ALERT_NOTIFICATIONS) }
        SettingsMenuItem("Privacy & Permissions") { onDestinationSelected(AppDestination.PRIVACY_PERMISSIONS) }
        SettingsMenuItem("Help & About") { onDestinationSelected(AppDestination.HELP_ABOUT) }
        SettingsMenuItem("Sign Out") {
            FirebaseAuth.getInstance().signOut()
            onSignOut()
        }
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
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp)
    )
}

