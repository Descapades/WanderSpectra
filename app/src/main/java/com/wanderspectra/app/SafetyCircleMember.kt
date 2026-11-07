package com.wanderspectra.app

/**
 * Represents a trusted individual in a child's WanderSpectra Safety Circle.
 *
 * Fields match the Safety Circle structure defined in the
 * WanderSpectra Design Document.
 */
data class SafetyCircleMember(
    val memberId: String = "",
    val caregiverId: String = "",
    val childId: String = "",
    val name: String = "",
    val relationship: String = "",
    val contactInformation: String = "",
    val isActive: Boolean = true,
    val elopementAlertsEnabled: Boolean = true,
    val locationAccess: String = "DURING_ELOPEMENT"
)
