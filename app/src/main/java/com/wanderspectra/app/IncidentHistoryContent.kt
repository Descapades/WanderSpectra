
package com.wanderspectra.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.wanderspectra.app.ui.theme.BackgroundCream
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * SCRUM-44: Reusable Incident History interface.
 *
 * Receives previously retrieved incidents for the selected child.
 * The calling screen is responsible for loading the records and
 * responding when the caregiver chooses View Report.
 *
 * This component does not access Firebase or modify incident data.
 */
@Composable
fun IncidentHistoryContent(
    incidents: List<IncidentRecord>,
    onViewReport: (IncidentRecord) -> Unit,
    childName: String = "",
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundCream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Incident History",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 28.sp
                )

                if (childName.isNotBlank()) {
                    Text(
                        text = childName,
                        color = PrimaryBlue,
                        fontFamily = Salsa,
                        fontSize = 20.sp
                    )
                }

                Text(
                    text = "Previously recorded elopement incidents",
                    color = PrimaryBlue,
                    fontSize = 15.sp
                )
            }
        }

        if (incidents.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            PrimaryBlue,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "No recorded incidents available for this child.",
                        color = PrimaryBlue,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            items(incidents) { incident ->
                IncidentHistoryCard(
                    incident = incident,
                    onViewReport = onViewReport
                )
            }
        }
    }
}

/**
 * Displays a summary of one stored incident.
 *
 * The full record is passed to the caller when View Report is selected.
 */
@Composable
private fun IncidentHistoryCard(
    incident: IncidentRecord,
    onViewReport: (IncidentRecord) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                PrimaryBlue,
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = formatIncidentDate(incident.startTime),
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 20.sp
        )

        Text(
            text = "Elopement Incident",
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 17.sp
        )

        IncidentHistoryDetail(
            label = "Start Time",
            value = formatIncidentTime(incident.startTime)
        )

        IncidentHistoryDetail(
            label = "End Time",
            value = formatIncidentTime(incident.endTime)
        )

        IncidentHistoryDetail(
            label = "Status",
            value = incident.status.ifBlank { "Not recorded" }
        )

        val coordinates =
            if (
                incident.lastKnownLatitude != null &&
                incident.lastKnownLongitude != null
            ) {
                "${incident.lastKnownLatitude}, " +
                    "${incident.lastKnownLongitude}"
            } else {
                "Not recorded"
            }

        IncidentHistoryDetail(
            label = "Last Known Coordinates",
            value = coordinates
        )

        TextButton(
            onClick = { onViewReport(incident) }
        ) {
            Text(
                text = "View Report >",
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Shared layout for a displayed incident field.
 */
@Composable
private fun IncidentHistoryDetail(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 14.sp
        )

        Text(
            text = value,
            color = PrimaryBlue,
            fontSize = 16.sp
        )
    }
}

private fun formatIncidentDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "Date not recorded"

    return SimpleDateFormat(
        "MMMM d, yyyy",
        Locale.getDefault()
    ).format(timestamp.toDate())
}

private fun formatIncidentTime(timestamp: Timestamp?): String {
    if (timestamp == null) return "Not recorded"

    return SimpleDateFormat(
        "h:mm a",
        Locale.getDefault()
    ).format(timestamp.toDate())
}
