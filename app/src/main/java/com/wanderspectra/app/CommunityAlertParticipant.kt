package com.wanderspectra.app

import com.google.firebase.Timestamp

data class CommunityAlertParticipant(
    val userId: String = "",
    val optedIn: Boolean = false,
    val fcmToken: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationUpdatedAt: Timestamp? = null
)