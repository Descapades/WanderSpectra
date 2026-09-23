package com.wanderspectra.app.model

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
    val contactInformation: String = ""
)
