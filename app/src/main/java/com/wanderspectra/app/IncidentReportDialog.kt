package com.wanderspectra.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import java.text.DateFormat

@Composable
fun IncidentReportDialog(report: IncidentRecord, onDismiss: () -> Unit) {
    fun time(value: Timestamp?): String = value?.let {
        DateFormat.getDateTimeInstance().format(it.toDate())
    } ?: "Not recorded"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elopement Incident Report") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Incident ID: ${report.incidentId}")
                Text("Child ID: ${report.childId}")
                Text("Status: ${report.status}")
                Text("Started: ${time(report.startTime)}")
                Text("Ended: ${time(report.endTime)}")
                val lat = report.lastKnownLatitude
                val lng = report.lastKnownLongitude
                Text(if (lat != null && lng != null) "Last known coordinates: $lat, $lng"
                    else "Last known coordinates: Not recorded")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
