package com.wanderspectra.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import com.wanderspectra.app.ui.theme.TanSongbird
import androidx.compose.ui.platform.LocalContext
import android.location.Geocoder
import android.os.Build
import androidx.compose.runtime.LaunchedEffect
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import com.wanderspectra.app.WearConnectionManager.publishSafeZoneDeletion
import com.wanderspectra.app.ui.theme.CardCream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.auth.FirebaseAuth

private val ZoneBlue = Color(0xFF4681B2)
private val ZoneRed = Color(0xFFC34A5B)
private val ZoneYellow = Color(0xFFFAE3A6)
private val ZoneCream = Color(0xFFEEE9D5)
private val ZonePinkButton = Color(0xFFE2A0A2)
private val ZoneBlueButton = Color(0xFFA9C8D9)

@Composable
fun SafeZoneScreen(
    onAddClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var safeZones by remember { mutableStateOf<List<SafeZone>>(emptyList()) }
    var selectedChildId by remember { mutableStateOf<String?>(null) }
    var childMonitoring by remember { mutableStateOf<ChildSafeZoneMonitoring?>(null) }
    var loading by remember { mutableStateOf(true) }
    var monitoring by remember { mutableStateOf(false) }
    var updatingMonitoring by remember { mutableStateOf(false) }
    var monitoringStatuses by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    val context = LocalContext.current
    var safeZoneAddresses by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    var zoneToDelete by remember { mutableStateOf<SafeZone?>(null) }
    var deletingSafeZone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        SafeZoneRepository().getAll { zones ->
            safeZones = zones
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        ChildProfileRepository().getChildProfiles(
            onSuccess = { children ->
                selectedChildId = children.firstOrNull()?.childId
            },
            onFailure = {
                selectedChildId = null
            }
        )
    }

    LaunchedEffect(selectedChildId) {
        val childId = selectedChildId

        if (childId.isNullOrBlank()) {
            childMonitoring = null
        } else {
            ChildSafeZoneMonitoringRepository().get(childId) { settings ->
                childMonitoring = settings
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(32.dp),
                    color = ZoneBlue
                )
            } else if (safeZones.isEmpty()) {
                Text(
                    text = "Safe Zones",
                    color = ZoneRed,
                    fontFamily = TanSongbird,
                    fontSize = 22.sp
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "No Safe Zone Created",
                    color = ZoneBlue,
                    fontFamily = Salsa,
                    fontSize = 20.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Add a Safe Zone to begin monitoring.",
                    color = ZoneBlue
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZoneBlue
                    )
                ) {
                    Text("Add Safe Zone")
                }
            } else {
                Text(
                    text = "Safe Zones",
                    color = ZoneRed,
                    fontFamily = TanSongbird,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                safeZones.forEach { zone ->
                    LaunchedEffect(zone.id, zone.lat, zone.lng) {
                        safeZoneAddresses = safeZoneAddresses + (
                                zone.id to "Loading address..."
                                )

                        val geocoder = Geocoder(context, Locale.getDefault())

                        try {
                            @Suppress("DEPRECATION")
                            val address = withContext(Dispatchers.IO) {
                                geocoder.getFromLocation(zone.lat, zone.lng, 1)
                                    ?.firstOrNull()
                                    ?.getAddressLine(0)
                            }

                            safeZoneAddresses = safeZoneAddresses + (
                                    zone.id to (address ?: "Address unavailable")
                                    )
                        } catch (e: Exception) {
                            safeZoneAddresses = safeZoneAddresses + (
                                    zone.id to "Address unavailable"
                                    )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = CardCream,
                        border = BorderStroke(1.dp, ZoneBlue)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = zone.name,
                                    color = ZoneBlue,
                                    fontFamily = TanSongbird,
                                    fontSize = 16.sp,
                                    modifier = Modifier.offset(x = (-16).dp)
                                )
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Switch(
                                        checked = childMonitoring?.let { settings ->
                                            settings.monitoringEnabled &&
                                            settings.activeSafeZoneId == zone.id
                                        } ?: false,
                                        enabled = !updatingMonitoring,
                                        onCheckedChange = { enabled ->
                                            val childId = selectedChildId

                                            if (childId.isNullOrBlank()) {
                                                monitoringStatuses = monitoringStatuses + (
                                                        zone.id to "No child profile selected."
                                                        )
                                            } else {
                                                updatingMonitoring = true

                                                monitoringStatuses = monitoringStatuses + (
                                                        zone.id to "Updating monitoring..."
                                                        )

                                                ChildSafeZoneMonitoringRepository().setMonitoring(
                                                    childId = childId,
                                                    safeZoneId = zone.id,
                                                    enabled = enabled
                                                ) { success, message ->

                                                    if (success) {
                                                        childMonitoring = ChildSafeZoneMonitoring(
                                                            childId = childId,
                                                            caregiverId = FirebaseAuth.getInstance()
                                                                .currentUser?.uid.orEmpty(),
                                                            activeSafeZoneId = if (enabled) zone.id else "",
                                                            monitoringEnabled = enabled
                                                        )

                                                        monitoringStatuses = monitoringStatuses + (
                                                                zone.id to "Monitoring saved. Syncing watch..."
                                                                )

                                                        WearConnectionManager.syncChildSafeZone(
                                                            context = context,
                                                            childId = childId,
                                                            safeZone = zone,
                                                            enabled = enabled
                                                        ) { synced ->

                                                            monitoringStatuses = monitoringStatuses + (
                                                                    zone.id to if (synced) {
                                                                        "Monitoring settings sent to watch."
                                                                    } else {
                                                                        "Monitoring saved. Watch not connected or sync failed."
                                                                    }
                                                                    )

                                                            updatingMonitoring = false
                                                        }
                                                    } else {
                                                        monitoringStatuses = monitoringStatuses + (
                                                                zone.id to message
                                                                )
                                                        updatingMonitoring = false
                                                    }
                                                }
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = ZoneBlue,
                                            checkedBorderColor = ZoneBlue,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = ZonePinkButton,
                                            uncheckedBorderColor = ZoneRed
                                        ),
                                        modifier = Modifier.height(40.dp)
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        text = if (
                                            childMonitoring?.monitoringEnabled == true &&
                                            childMonitoring?.activeSafeZoneId == zone.id
                                        ) "Safe Zone On" else "Safe Zone Off",

                                        color = if (
                                            childMonitoring?.monitoringEnabled == true &&
                                            childMonitoring?.activeSafeZoneId == zone.id
                                        ) ZoneBlue else ZoneRed,
                                        fontFamily = Salsa,
                                        fontSize = 16.sp
                                    )
                                }

                                val zoneStatus = monitoringStatuses[zone.id].orEmpty()

                                if (zoneStatus.isNotEmpty()) {
                                    Text(
                                        text = zoneStatus,
                                        color = ZoneBlue,
                                        fontFamily = Salsa,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                ZoneDetail(
                                    label = "Radius:",
                                    value = "${zone.radius} feet"
                                )

                                ZoneDetail(
                                    label = "Address:",
                                    value = safeZoneAddresses[zone.id] ?: "Loading address..."
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(Modifier.height(12.dp))

                                SafeZoneMap(
                                    latitude = zone.lat,
                                    longitude = zone.lng,
                                    radiusFeet = zone.radius
                                )
                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = { zoneToDelete = zone },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ZonePinkButton,
                                            contentColor = SecondaryRed
                                        ),
                                        border = BorderStroke(1.dp, SecondaryRed)
                                    ) {
                                        Text(
                                            "Delete",
                                            fontFamily = Salsa,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Button(
                                        onClick = { onEditClick(zone.id) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ZoneBlueButton,
                                            contentColor = SecondaryBlue
                                        ),
                                        border = BorderStroke(1.dp, SecondaryBlue)
                                    ) {
                                        Text(
                                            "Edit",
                                            fontFamily = Salsa,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                }
                 Button(
                    onClick = onAddClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZoneBlueButton,
                        contentColor = SecondaryBlue
                    ),
                    border = BorderStroke(1.dp, SecondaryBlue)
                ) {
                    Text(
                        text = "Add Another",
                        fontFamily = Salsa,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (zoneToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        zoneToDelete = null
                    },
                    title = {
                        Text("Delete Safe Zone?")
                    },
                    text = {
                        Text(
                            "Are you sure you want to delete this Safe Zone? " +
                                    "This action cannot be undone."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            enabled = !deletingSafeZone,
                            onClick = {
                                val selectedZone = zoneToDelete

                                if (selectedZone != null && !deletingSafeZone) {
                                    deletingSafeZone = true

                                    SafeZoneRepository().delete(selectedZone.id) { success, message ->
                                        deletingSafeZone = false

                                        if (success) {
                                            safeZones =
                                                safeZones.filter { it.id != selectedZone.id }
                                            monitoringStatuses = monitoringStatuses - selectedZone.id
                                            zoneToDelete = null

                                            publishSafeZoneDeletion(
                                                context = context,
                                                zoneId = selectedZone.id
                                            ) { synced ->
                                                if (!synced) {
                                                    monitoringStatuses = monitoringStatuses + (
                                                            selectedZone.id to "Safe Zone deleted, but watch synchronization failed."
                                                            )
                                                }
                                            }
                                        } else {
                                            monitoringStatuses = monitoringStatuses + (
                                                    selectedZone.id to message
                                                    )
                                            zoneToDelete = null
                                        }
                                    }
                                }
                            }
                        ) {
                            if (deletingSafeZone) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = ZoneRed
                                )
                            } else {
                                Text("Delete", color = ZoneRed)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                zoneToDelete = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
    @Composable
    private fun ZoneDetail(
        label: String,
        value: String
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$label ",
                color = ZoneRed,
                fontFamily = Salsa,
                fontSize = 20.sp
            )

            Text(
                text = value,
                color = ZoneBlue,
                fontFamily = Salsa,
                fontSize = 20.sp
            )
        }
    }
