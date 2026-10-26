package com.wanderspectra.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.*

@Composable
fun ElopementModeContent(
    incidentId: String?,
    statusText: String,
    pointLat: Double = 0.0,
    pointLng: Double = 0.0,
    childName: String = "",
    onChildSafe: (String) -> Unit,
    onBackHome: () -> Unit,
    isBusy: Boolean = false,
    caregiverId: String = "",
    childId: String = "",
    onEmergencyProfile: (() -> Unit)? = null,
    emergencyProfileAvailable: Boolean = false,
    onSafetyCircle: (() -> Unit)? = null,
    onCommunity: (() -> Unit)? = null
) {
    var showLocation by remember(incidentId) { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ELOPEMENT MODE ACTIVE", color = SecondaryRed, fontFamily = Salsa,
            fontSize = 22.sp, textAlign = TextAlign.Center)
        Text("$childName is missing", color = PrimaryBlue, fontFamily = Salsa, fontSize = 18.sp)
        Text(statusText, fontFamily = Salsa, textAlign = TextAlign.Center)
        incidentId?.let { Text("Incident: $it", fontFamily = Salsa, fontSize = 12.sp) }
        Button(onClick = { showLocation = !showLocation }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showLocation) "Hide Wearable Location" else "View Wearable Location", fontFamily = Salsa)
        }
        if (showLocation) {
            EmergencyWearableLocation(caregiverId = caregiverId, childId = childId)
        }
        onEmergencyProfile?.let { action ->
            Button(onClick = action, enabled = emergencyProfileAvailable, modifier = Modifier.fillMaxWidth()) {
                Text("View Emergency Profile", fontFamily = Salsa)
            }
            if (!emergencyProfileAvailable) {
                Text("Emergency profile unavailable. The active child's profile must finish loading.", fontFamily = Salsa)
            }
        }
        EmergencyServicesShortcut()
        onSafetyCircle?.let { action ->
            Button(onClick = action, modifier = Modifier.fillMaxWidth()) {
                Text("Safety Circle", fontFamily = Salsa)
            }
        }
        onCommunity?.let { action ->
            Button(onClick = action, modifier = Modifier.fillMaxWidth()) {
                Text("Community Alerts", fontFamily = Salsa)
            }
        }
        Button(
            onClick = { incidentId?.let(onChildSafe) },
            enabled = incidentId != null && !isBusy,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue, contentColor = SecondaryBlue)
        ) { Text(if (isBusy) "Saving..." else "Child is Safe", fontFamily = Salsa) }
        Button(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Home", fontFamily = Salsa)
        }
    }
}
