package com.wanderspectra.app

import com.google.firebase.Timestamp

data class IncidentRecord(
    val incidentId: String = "",
    val childId: String = "",
    val caregiverId: String = "",
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val status: String = "",
    val lastKnownLatitude: Double? = null,
    val lastKnownLongitude: Double? = null,
    val type: String = "ELOPEMENT",
    val safeZoneId: String? = null,
    val safeZoneName: String? = null,
    val resolutionType: String? = null,
    val displacementMeters: Double? = null,
    val distanceTraveledMeters: Double? = null,
    val routePoints: List<Map<String, Any>> = emptyList(),
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
    val notes: String = "",
    val possibleTrigger: String = ""
)
