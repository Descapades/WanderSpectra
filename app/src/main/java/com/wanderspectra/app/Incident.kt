package com.wanderspectra.app

data class Incident(
    val id: String = "",
    val childId: String = "",
    val caregiverId: String = "",
    val status: String = "open",
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val lastKnownLat: Double = 0.0,
    val lastKnownLng: Double = 0.0
)