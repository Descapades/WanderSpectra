
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
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import android.content.Context
import android.location.Geocoder
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.TanSongbird

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
                    color = EmergencyRed,
                    fontFamily = TanSongbird,
                    fontSize = 20.sp
                )

                if (childName.isNotBlank()) {
                    Text(
                        text = childName,
                        color = PrimaryBlue,
                        fontFamily = TanSongbird,
                        fontSize = 16.sp
                    )
                }
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
    val context = LocalContext.current

    var destinationAddress by remember(incident.incidentId) {
        mutableStateOf("Not recorded")
    }

    LaunchedEffect(
        incident.incidentId,
        incident.lastKnownLatitude,
        incident.lastKnownLongitude,
        incident.routePoints
    ) {
        val startLatitude = incident.lastKnownLatitude
        val startLongitude = incident.lastKnownLongitude

        val farthestPoint = if (
            startLatitude != null &&
            startLongitude != null
        ) {
            incident.routePoints.maxByOrNull { point ->
                val pointLatitude = point["latitude"] as? Double
                val pointLongitude = point["longitude"] as? Double

                if (pointLatitude == null || pointLongitude == null) {
                    0f
                } else {
                    val results = FloatArray(1)

                    android.location.Location.distanceBetween(
                        startLatitude,
                        startLongitude,
                        pointLatitude,
                        pointLongitude,
                        results
                    )

                    results[0]
                }
            }
        } else {
            null
        }

        val latitude = farthestPoint?.get("latitude") as? Double
        val longitude = farthestPoint?.get("longitude") as? Double

        destinationAddress = if (
            latitude != null &&
            longitude != null &&
            latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0
        ) {
            withContext(Dispatchers.IO) {
                getDestinationAddress(context, latitude, longitude)
            }
        } else {
            "Not recorded"
        }
    }

    val durationMinutes =
        if (incident.startTime != null && incident.endTime != null) {
            ((incident.endTime.toDate().time -
                    incident.startTime.toDate().time) / 60000L)
                .coerceAtLeast(0L)
        } else {
            null
        }

    val statusText = when (incident.status.lowercase()) {
        "open", "active" -> "Active"
        "closed", "resolved", "safe" -> "Resolved"
        else -> "Not recorded"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = CardCream,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                PrimaryBlue,
                RoundedCornerShape(8.dp)
            )
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 12.dp,
                bottom = 4.dp
            ),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = formatIncidentDate(incident.startTime),
            color = EmergencyRed,
            fontFamily = Salsa,
            fontSize = 20.sp
        )

        Text(
            text = when (incident.type) {
                "SAFE_ZONE_EXIT" -> "Safe Zone Exit"
                else -> "Elopement Incident"
            },
            color = EmergencyRed,
            fontFamily = Salsa,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 14.dp)
        )

        Text(
            text = "${formatIncidentTime(incident.startTime)} – " +
                    if (incident.status.lowercase() in listOf("open", "active")) {
                        "Now"
                    } else {
                        formatIncidentTime(incident.endTime)
                    },
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 14.dp)
        )

        Row(
            modifier = Modifier.padding(start = 14.dp)
        ) {
            Text(
                text = "Duration: ",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 20.sp
            )

            Text(
                text = if (
                    incident.type == "SAFE_ZONE_EXIT" &&
                    incident.status.lowercase() in listOf("open", "active")
                ) {
                    "In progress"
                } else {
                    durationMinutes?.let { "$it minutes" } ?: "Not recorded"
                },
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp
            )
        }

        Row(
            modifier = Modifier.padding(start = 14.dp)
        ) {
            Text(
                text = "Safe Zone: ",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 20.sp
            )

            Text(
                text = if (incident.type == "SAFE_ZONE_EXIT") {
                    incident.safeZoneName?.takeIf { it.isNotBlank() }
                        ?: "Not recorded"
                } else {
                    "Not recorded"
                },
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp
            )
        }

        val distanceText = incident.distanceTraveledMeters?.let { meters ->
            val miles = meters / 1609.344

            String.format(
                java.util.Locale.US,
                "%.2f miles",
                miles
            )
        } ?: "Not recorded"

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append("Distance Traveled: ")
                }
                withStyle(SpanStyle(color = PrimaryBlue)) {
                    append(distanceText)
                }
            },
            fontFamily = Salsa,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 14.dp)
        )

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append("Farthest Location: ")
                }
                withStyle(SpanStyle(color = PrimaryBlue)) {
                    append(destinationAddress)
                }
            },
            fontFamily = Salsa,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 14.dp)
        )

        Row(
            modifier = Modifier.padding(start = 14.dp)
        ) {
            Text(
                text = "Status: ",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 20.sp
            )

            Text(
                text = statusText,
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp
            )
        }

        if (incident.status.lowercase() in listOf("closed", "resolved", "safe")) {
            val resolutionText = when (incident.resolutionType) {
                "RETURNED_TO_SAFE_ZONE" -> "Returned to Safe Zone"
                "CAREGIVER_CONFIRMED_SAFE" -> "Manually Marked Safe"
                else -> "Not recorded"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Resolution:",
                    color = EmergencyRed,
                    fontFamily = Salsa,
                    fontSize = 20.sp
                )

                Text(
                    text = resolutionText,
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { onViewReport(incident) },
                contentPadding = PaddingValues(
                    horizontal = 8.dp,
                    vertical = 0.dp
                )
            ) {
                Text(
                    text = "View Report >",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 20.sp
                )
            }
        }
    }
}

private fun getDestinationAddress(
    context: Context,
    latitude: Double,
    longitude: Double
): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())

        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        )

        addresses?.firstOrNull()?.getAddressLine(0)
            ?: "Address unavailable"
    } catch (exception: Exception) {
        "Address unavailable"
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
