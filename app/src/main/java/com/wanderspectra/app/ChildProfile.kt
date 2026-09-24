package com.wanderspectra.app

data class ChildProfile(
    val childId: String = "",
    val caregiverUid: String = "",

    // Basic Information
    val fullName: String = "",
    val preferredName: String = "",
    val birthdate: String = "",
    val photoUrl: String = "",
    val height: String = "",
    val weight: String = "",
    val hairColor: String = "",
    val eyeColor: String = "",

    // Communication
    val communicationStyle: String = "",
    val helpfulCommunicationInfo: String = "",

    // Emergency Information
    val allergies: String = "",
    val medicalConsiderations: String = "",
    val criticalInformation: String = "",

    // Safety and Sensory Information
    val sensorySensitivities: String = "",
    val elopementTriggers: String = "",
    val calmingStrategies: String = "",
    val safetyConcerns: String = "",

    // Wear OS Safety Prompt
    val safetyPrompt: String = ""
)