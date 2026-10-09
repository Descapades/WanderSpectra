
package com.wanderspectra.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * SCRUM-39: Debug-only previews for independent visual verification.
 *
 * All information below is fictional sample data.
 * This file is not part of the production app.
 */

@Preview(
    name = "Emergency Profile - Populated",
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)
@Composable
private fun EmergencyProfilePopulatedPreview() {
    EmergencyProfileContent(
        profile = ChildProfile(
            fullName = "Alex Morgan",
            preferredName = "Alex",
            birthdate = "04/15/2018",
            height = "48 inches",
            weight = "55 lbs",
            hairColor = "Brown",
            eyeColor = "Blue",
            communicationStyle = "Uses short verbal phrases",
            helpfulCommunicationInfo =
                "Speak calmly and use simple, direct instructions.",
            allergies = "Peanuts",
            medicalConsiderations =
                "Emergency medication information is available from caregiver.",
            criticalInformation =
                "May approach roads without recognizing traffic hazards.",
            sensorySensitivities = "Sensitive to loud noises",
            elopementTriggers = "Crowded or unfamiliar environments",
            calmingStrategies =
                "Use a quiet voice and allow personal space.",
            safetyConcerns = "May move toward nearby water",
            safetyPrompt = "Stop and wait for your caregiver."
        ),
        onBack = {}
    )
}

@Preview(
    name = "Emergency Profile - Missing Information",
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)
@Composable
private fun EmergencyProfileMissingInformationPreview() {
    EmergencyProfileContent(
        profile = ChildProfile(
            fullName = "Sample Child",
            preferredName = "Sam"
        ),
        onBack = {}
    )
}

@Preview(
    name = "Emergency Profile - No Profile",
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)
@Composable
private fun EmergencyProfileEmptyPreview() {
    EmergencyProfileContent(
        profile = null,
        onBack = {}
    )
}
