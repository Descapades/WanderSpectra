package com.wanderspectra.app

import com.google.firebase.Timestamp

data class CommunityAlert(
    val id: String = "",
    val incidentId: String = "",
    val caregiverId: String = "",
    val childId: String = "",
    val childName: String = "",
    val photoUrl: String = "",
    val status: String = "ACTIVE",
    val distanceMiles: Double? = null,
    val lastKnownArea: String = "",
    val approximateLatitude: Double? = null,
    val approximateLongitude: Double? = null,
    val locationTimestamp: Long = 0L,
    val isLiveLocation: Boolean = false,
    val alertRadiusKm: Double = 1.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val resolvedAt: Timestamp? = null
)
