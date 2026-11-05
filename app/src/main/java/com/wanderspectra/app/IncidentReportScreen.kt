package com.wanderspectra.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.wanderspectra.app.ui.theme.BackgroundCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun IncidentReportScreen(
    report: IncidentRecord,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationMinutes =
        if (report.startTime != null && report.endTime != null) {
            ((report.endTime.toDate().time -
                    report.startTime.toDate().time) / 60000L)
                .coerceAtLeast(0L)
        } else {
            null
        }

    val statusText = when (report.status.lowercase()) {
        "closed", "resolved", "safe" -> "Child Found Safe"
        "open", "active" -> "Active Incident"
        else -> report.status.ifBlank { "Not recorded" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(
                text = "< Back to History",
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 16.sp
            )
        }

        Text(
            text = "Incident Report",
            color = EmergencyRed,
            fontFamily = Salsa,
            fontSize = 30.sp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    PrimaryBlue,
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = report.startTime?.let {
                    SimpleDateFormat(
                        "MMMM d, yyyy",
                        Locale.getDefault()
                    ).format(it.toDate())
                } ?: "Date not recorded",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 22.sp
            )

            Text(
                text = "Elopement Incident",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 19.sp
            )

            ReportDetail(
                label = "Time",
                value = "${formatReportTime(report.startTime)} – " +
                        formatReportTime(report.endTime)
            )

            ReportDetail(
                label = "Duration",
                value = durationMinutes?.let { "$it minutes" }
                    ?: "Not recorded"
            )

            ReportDetail(
                label = "Safe Zone",
                value = "Not recorded"
            )

            ReportDetail(
                label = "Distance Traveled",
                value = "Not recorded"
            )

            ReportDetail(
                label = "Status",
                value = statusText
            )

            ReportDetail(
                label = "Last Known Coordinates",
                value = if (
                    report.lastKnownLatitude != null &&
                    report.lastKnownLongitude != null
                ) {
                    "${report.lastKnownLatitude}, ${report.lastKnownLongitude}"
                } else {
                    "Not recorded"
                }
            )

            Text(
                text = "Response Timeline",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 22.sp
            )

            Text(
                text = "No response events recorded.",
                color = PrimaryBlue,
                fontSize = 15.sp
            )

            Text(
                text = "Notes",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 22.sp
            )

            Text(
                text = "No notes recorded.",
                color = PrimaryBlue,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun ReportDetail(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "$label:",
            color = EmergencyRed,
            fontFamily = Salsa,
            fontSize = 16.sp
        )

        Text(
            text = value,
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 16.sp
        )
    }
}

private fun formatReportTime(timestamp: Timestamp?): String {
    return timestamp?.let {
        SimpleDateFormat(
            "h:mm a",
            Locale.getDefault()
        ).format(it.toDate())
    } ?: "Not recorded"
}

