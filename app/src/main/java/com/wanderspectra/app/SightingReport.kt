package com.wanderspectra.app

import com.google.firebase.Timestamp

data class SightingReport(
    val id: String = "",
    val communityAlertId: String = "",
    val reporterId: String = "",
    val childId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationDescription: String = "",
    val sightingTime: Timestamp? = null,
    val direction: String = "",
    val details: String = "",
    val submittedAt: Timestamp? = null
)