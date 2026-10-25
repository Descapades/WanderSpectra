package com.wanderspectra.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed

@Composable
fun ElopementModeContent(
    incidentId: String?,
    statusText: String,
    pointLat: Double,
    pointLng: Double,
    childName: String = "Jimothy",
    onChildSafe: (String) -> Unit,
    onBackHome: () -> Unit,
    isBusy: Boolean = false
) {
    var zoneText by remember { mutableStateOf("Checking zone...") }

    LaunchedEffect(pointLat, pointLng) {
        SafeZoneRepository().getLatest { zone ->
            zoneText = if (zone == null) {
                "No saved zone"
            } else if (ZoneCheck.isOutside(zone, pointLat, pointLng)) {
                "Outside safe zone"
            } else {
                "Inside safe zone"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ELOPEMENT MODE ACTIVE",
            color = SecondaryRed,
            fontFamily = Salsa,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "$childName is missing", color = PrimaryBlue, fontFamily = Salsa, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = zoneText, color = SecondaryRed, fontFamily = Salsa, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = statusText, fontFamily = Salsa, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        EmergencyServicesShortcut()
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { if (incidentId != null) onChildSafe(incidentId) },
            enabled = incidentId != null && !isBusy,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue, contentColor = SecondaryBlue)
        ) { Text("Child is Safe", fontFamily = Salsa) }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Home", fontFamily = Salsa)
        }
    }
}
