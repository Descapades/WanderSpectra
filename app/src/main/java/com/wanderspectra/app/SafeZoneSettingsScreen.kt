package com.wanderspectra.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
    var name by remember { mutableStateOf("Home") }
    var radiusText by remember { mutableStateOf("500") }
    var latText by remember { mutableStateOf("30.43") }
    var lngText by remember { mutableStateOf("-86.57") }
    var monitoring by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("Not saved yet") }
    LaunchedEffect(Unit) {
        SafeZoneRepository().getLatest { zone ->
            if (zone != null) {
                name = zone.name
                radiusText = zone.radius.toString()
                latText = zone.lat.toString()
                lngText = zone.lng.toString()
                monitoring = zone.monitoringEnabled
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
            text = "Safe Zone Settings",
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 24.sp
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Zone name") },
            modifier = Modifier.padding(top = 16.dp)
        )
        OutlinedTextField(
            value = radiusText,
            onValueChange = { radiusText = it },
            label = { Text("Radius (ft)") },
            modifier = Modifier.padding(top = 8.dp)
        )
        OutlinedTextField(
            value = latText,
            onValueChange = { latText = it },
            label = { Text("Latitude") },
            modifier = Modifier.padding(top = 8.dp)
        )
        OutlinedTextField(
            value = lngText,
            onValueChange = { lngText = it },
            label = { Text("Longitude") },
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Monitoring", fontFamily = Salsa)
            Switch(
                checked = monitoring,
                onCheckedChange = { monitoring = it },
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(
            text = status,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            onClick = {
                if (name.isBlank()) {
                    status = "Name cannot be empty"
                    return@Button
                }
                val lat = latText.toDoubleOrNull()
                val lng = lngText.toDoubleOrNull()
                if (lat == null || lng == null) {
                    status = "Lat and lng must be numbers"
                    return@Button
                }
                val zone = SafeZone(
                    name = name,
                    childId = "test-child",
                    radius = radiusText.toIntOrNull() ?: 500,
                    monitoringEnabled = monitoring,
                    lat = lat,
                    lng = lng
                )
                SafeZoneRepository().save(zone) { _, message ->
                    status = message
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save zone")
        }
    }
}