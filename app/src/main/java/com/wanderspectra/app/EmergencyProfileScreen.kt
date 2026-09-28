
package com.wanderspectra.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wanderspectra.app.ui.theme.BackgroundCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa

/**
 * SCRUM-39: Reusable, read-only Emergency Profile interface.
 *
 * The calling screen provides the child's information and handles
 * navigation. This component does not access Firebase or manage
 * Elopement Mode.
 */
@Composable
fun EmergencyProfileContent(
    profile: ChildProfile?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        TextButton(onClick = onBack) {
            Text(
                text = "Back",
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 16.sp
            )
        }

        Text(
            text = "Emergency Profile",
            color = EmergencyRed,
            fontFamily = Salsa,
            fontSize = 28.sp
        )

        Text(
            text = "Important child safety, communication, and emergency information",
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 16.sp
        )

        if (profile == null) {
            EmergencyProfileSection("Profile Information") {
                EmergencyProfileDetail(
                    label = "Status",
                    value = "No child profile is currently available."
                )
            }
            return@Column
        }

        EmergencyProfileHeader(profile)

        EmergencyProfileSection("Emergency Information") {
            EmergencyProfileDetail(
                "Critical Information",
                profile.criticalInformation,
                emphasize = true
            )
            EmergencyProfileDetail("Allergies", profile.allergies)
            EmergencyProfileDetail(
                "Medical Considerations",
                profile.medicalConsiderations
            )
        }

        EmergencyProfileSection("Communication") {
            EmergencyProfileDetail(
                "Communication Style",
                profile.communicationStyle
            )
            EmergencyProfileDetail(
                "Helpful Communication Information",
                profile.helpfulCommunicationInfo
            )
        }

        EmergencyProfileSection("Safety and Sensory Information") {
            EmergencyProfileDetail(
                "Safety Concerns",
                profile.safetyConcerns,
                emphasize = true
            )
            EmergencyProfileDetail(
                "Elopement Triggers",
                profile.elopementTriggers
            )
            EmergencyProfileDetail(
                "Sensory Sensitivities",
                profile.sensorySensitivities
            )
            EmergencyProfileDetail(
                "Calming Strategies",
                profile.calmingStrategies
            )
        }

        EmergencyProfileSection("Identifying Information") {
            EmergencyProfileDetail("Full Name", profile.fullName)
            EmergencyProfileDetail(
                "Preferred Name",
                profile.preferredName
            )
            EmergencyProfileDetail("Birthdate", profile.birthdate)
            EmergencyProfileDetail("Height", profile.height)
            EmergencyProfileDetail("Weight", profile.weight)
            EmergencyProfileDetail("Hair Color", profile.hairColor)
            EmergencyProfileDetail("Eye Color", profile.eyeColor)
        }

        EmergencyProfileSection("Wear OS Safety Prompt") {
            EmergencyProfileDetail(
                "Configured Prompt",
                profile.safetyPrompt
            )
        }
    }
}

/**
 * Displays identifying information and the child's photograph.
 */
@Composable
private fun EmergencyProfileHeader(profile: ChildProfile) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (profile.photoUrl.isNotBlank()) {
            AsyncImage(
                model = profile.photoUrl,
                contentDescription = if (profile.fullName.isNotBlank()) {
                    "Photo of ${profile.fullName}"
                } else {
                    "Child profile photo"
                },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(108.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .background(
                        color = BackgroundCream,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No photo",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 14.sp
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = profile.fullName.ifBlank {
                    "Name not provided"
                },
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 22.sp
            )

            if (profile.preferredName.isNotBlank()) {
                Text(
                    text = "Preferred name: ${profile.preferredName}",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Shared section layout for Emergency Profile information.
 */
@Composable
private fun EmergencyProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BackgroundCream,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = PrimaryBlue,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 20.sp
        )

        content()
    }
}

/**
 * Displays an individual label and value.
 * Missing information is explicitly identified.
 */
@Composable
private fun EmergencyProfileDetail(
    label: String,
    value: String,
    emphasize: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            color = if (emphasize) EmergencyRed else PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 15.sp
        )

        Text(
            text = value.ifBlank { "Not provided" },
            color = if (emphasize && value.isNotBlank()) {
                EmergencyRed
            } else {
                PrimaryBlue
            },
            fontSize = 16.sp
        )
    }
}
