package com.wanderspectra.app

import com.google.firebase.firestore.PropertyName


data class SafetyCircleMember(
    val memberId: String = "",
    val caregiverId: String = "",
    val childId: String = "",
    val name: String = "",
    val relationship: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    @get:PropertyName("active")
    @field:PropertyName("active")
    val isActive: Boolean = true,
    val elopementAlertsEnabled: Boolean = true,
    val locationAccess: String = "DURING_ELOPEMENT"
)
