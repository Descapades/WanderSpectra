package com.wanderspectra.app

import com.google.firebase.Timestamp

/**
 * SCRUM-44: Represents a recorded WanderSpectra elopement incident.
 *
 * Based on the incident structure in the WanderSpectra Design Document.
 *
 * incidentId represents the Firestore document ID and can be populated
 * by the repository when retrieving a stored incident.
 *
 * Nullable timestamps and coordinates allow the interface to distinguish
 * missing information from actual recorded values.
 */
data class IncidentRecord(
    val incidentId: String = "",
    val childId: String = "",
    val caregiverId: String = "",
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val status: String = "",
    val lastKnownLatitude: Double? = null,
    val lastKnownLongitude: Double? = null
)
