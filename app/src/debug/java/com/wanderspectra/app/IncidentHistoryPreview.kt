
package com.wanderspectra.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * SCRUM-44: Debug-only Incident History previews.
 *
 * All incident records below contain fictional test information.
 * These previews do not access Firebase or modify stored incidents.
 */

@Preview(
    name = "Incident History - Populated",
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)
@Composable
private fun IncidentHistoryPopulatedPreview() {
    IncidentHistoryContent(
        childName = "Alex Morgan",
        incidents = listOf(
            IncidentRecord(
                incidentId = "sample-incident-1",
                caregiverId = "sample-caregiver",
                childId = "sample-child",
                startTime = sampleTimestamp(
                    2026, Calendar.SEPTEMBER, 12, 15, 42
                ),
                endTime = sampleTimestamp(
                    2026, Calendar.SEPTEMBER, 12, 15, 56
                ),
                status = "Child Found Safe",
                lastKnownLatitude = 27.94,
                lastKnownLongitude = -82.46
            ),
            IncidentRecord(
                incidentId = "sample-incident-2",
                caregiverId = "sample-caregiver",
                childId = "sample-child",
                startTime = sampleTimestamp(
                    2026, Calendar.AUGUST, 27, 11, 18
                ),
                endTime = sampleTimestamp(
                    2026, Calendar.AUGUST, 27, 11, 24
                ),
                status = "Child Found Safe",
                lastKnownLatitude = 27.95,
                lastKnownLongitude = -82.45
            )
        ),
        onViewReport = {}
    )
}

@Preview(
    name = "Incident History - Missing Information",
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)
@Composable
private fun IncidentHistoryMissingInformationPreview() {
    IncidentHistoryContent(
        childName = "Sample Child",
        incidents = listOf(
            IncidentRecord(
                incidentId = "sample-incomplete-incident",
                caregiverId = "sample-caregiver",
                childId = "sample-child",
                startTime = sampleTimestamp(
                    2026, Calendar.SEPTEMBER, 12, 15, 42
                )
            )
        ),
        onViewReport = {}
    )
}

@Preview(
    name = "Incident History - Empty",
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)
@Composable
private fun IncidentHistoryEmptyPreview() {
    IncidentHistoryContent(
        childName = "Sample Child",
        incidents = emptyList(),
        onViewReport = {}
    )
}

/**
 * Creates timestamps for fictional preview records.
 */
private fun sampleTimestamp(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int
): Timestamp {
    val calendar = GregorianCalendar(
        year,
        month,
        day,
        hour,
        minute
    )

    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)

    return Timestamp(calendar.time)
}
