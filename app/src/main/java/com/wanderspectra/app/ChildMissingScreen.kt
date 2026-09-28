package com.wanderspectra.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed

@Composable
fun ChildMissingContent() {
    var status by remember { mutableStateOf("Confirm this is an elopement") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Child is Missing?",
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 24.sp
        )
        Text(
            text = status,
            fontFamily = Salsa,
            modifier = Modifier.padding(top = 16.dp)
        )
        Row(
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Button(
                onClick = {
                    val incident = Incident(
                        childId = "test-child",
                        caregiverId = "test-caregiver",
                        status = "open",
                        startTime = System.currentTimeMillis(),
                        lastKnownLat = 30.43,
                        lastKnownLng = -86.57
                    )
                    IncidentRepository().startIncident(incident) { _, message ->
                        status = message
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonRed,
                    contentColor = SecondaryRed
                )
            ) {
                Text("Confirm", fontFamily = Salsa)
            }
            Button(
                onClick = {
                    status = "Cancelled. No incident written."
                },
                modifier = Modifier.padding(start = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBlue,
                    contentColor = SecondaryBlue
                )
            ) {
                Text("Cancel", fontFamily = Salsa)
            }
        }
    }
}