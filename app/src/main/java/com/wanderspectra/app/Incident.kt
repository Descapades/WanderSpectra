package com.wanderspectra.app

data class Incident(
    val id: String = "",
    val childId: String = "",
    val caregiverId: String = "",
    val status: String = "open",
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val lastKnownLat: Double? = null,
    val lastKnownLng: Double? = null
)
