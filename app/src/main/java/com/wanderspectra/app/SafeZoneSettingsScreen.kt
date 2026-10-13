package com.wanderspectra.app

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa

@Composable
fun SafeZoneSettingsContent() {
    var name by remember { mutableStateOf("Home") }
    var radiusText by remember { mutableStateOf("500") }
    var monitoring by remember { mutableStateOf(true) }
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
                val radius = radiusText.toIntOrNull() ?: 500
                val zone = SafeZone(
                    name = name,
                    childId = "test-child",
                    radius = radius,
                    monitoringEnabled = monitoring,
                    lat = 30.43,
                    lng = -86.57
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

@Preview(showBackground = true)
@Composable
fun SafeZoneSettingsPreview() {
    SafeZoneSettingsContent()
}