package com.wanderspectra.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa

@Composable
fun SafeZoneSettingsContent() {
    var status by remember { mutableStateOf("Not saved yet") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Safe Zone Settings",
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 24.sp
        )
        Text(
            text = status,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            onClick = {
                val zone = SafeZone(
                    name = "Home",
                    childId = "test-child",
                    radius = 500,
                    monitoringEnabled = true,
                    lat = 30.43,
                    lng = -86.57
                )
                SafeZoneRepository().save(zone) { ok ->
                    status = if (ok) "Saved to Firestore" else "Save failed"
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save test zone")
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SafeZoneSettingsPreview() {
    SafeZoneSettingsContent()
}