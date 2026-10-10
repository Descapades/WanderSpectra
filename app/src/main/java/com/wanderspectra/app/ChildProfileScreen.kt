package com.wanderspectra.app


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun ChildProfileContent() {

    val repository = remember {
        ChildProfileRepository()
    }

    var childProfile by remember {
        mutableStateOf<ChildProfile?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var isEditing by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        repository.getChildProfiles(
            onSuccess = { children ->
                childProfile = children.firstOrNull()
                isLoading = false
            },
            onFailure = { exception ->
                errorMessage = exception.message
                isLoading = false
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    color = PrimaryBlue
                )
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Unable to load child profile.",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 16.sp
                )
            }

            childProfile == null -> {
                Text(
                    text = "No child profile found.",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 16.sp
                )
            }

            else -> {
                val child = childProfile!!

                if (isEditing) {
                    EditChildProfile(
                        child = child,
                        onCancel = {
                            isEditing = false
                        },
                        onSave = { updatedChild ->
                            repository.updateChildProfile(
                                childProfile = updatedChild,
                                onSuccess = {
                                    childProfile = updatedChild
                                    isEditing = false
                                },
                                onFailure = { exception ->
                                    errorMessage = exception.message
                                }
                            )
                        }
                    )
                } else {
                    ViewChildProfile(
                        child = child,
                        onEdit = {
                            isEditing = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewChildProfile(
    child: ChildProfile,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = child.preferredName.ifBlank {
                child.fullName
            },
            color = EmergencyRed,
            fontFamily = Salsa,
            fontSize = 28.sp
        )
        if (child.photoUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))

            AsyncImage(
                model = child.photoUrl,
                contentDescription = "${child.preferredName.ifBlank { child.fullName }} profile picture",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Basic Information:")

        ProfileField(
            label = "Full Name:",
            value = child.fullName
        )

        ProfileField(
            label = "Preferred Name:",
            value = child.preferredName
        )

        ProfileField(
            label = "Birthdate:",
            value = child.birthdate
        )

        ProfileField(
            label = "Height:",
            value = child.height
        )

        ProfileField(
            label = "Weight:",
            value = child.weight
        )

        ProfileField(
            label = "Hair Color:",
            value = child.hairColor
        )

        ProfileField(
            label = "Eye Color:",
            value = child.eyeColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Communication:")

        ProfileField(
            label = "Communication Style:",
            value = child.communicationStyle
        )

        ProfileField(
            label = "Helpful Communication Information:",
            value = child.helpfulCommunicationInfo
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Safety & Sensory Information:")

        ProfileField(
            label = "Sensory Sensitivities:",
            value = child.sensorySensitivities
        )

        ProfileField(
            label = "Common Elopement Triggers:",
            value = child.elopementTriggers
        )

        ProfileField(
            label = "Calming Strategies:",
            value = child.calmingStrategies
        )

        ProfileField(
            label = "Safety Concerns:",
            value = child.safetyConcerns
        )

        ProfileField(
            label = "Safety Prompt:",
            value = child.safetyPrompt
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Emergency Information:")

        ProfileField(
            label = "Allergies:",
            value = child.allergies
        )

        ProfileField(
            label = "Medical Considerations:",
            value = child.medicalConsiderations
        )

        ProfileField(
            label = "Critical Information:",
            value = child.criticalInformation
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue,
                contentColor = SecondaryBlue
            )
        ) {
            Text(
                text = "Edit Child Profile",
                fontFamily = Salsa,
                fontSize = 16.sp,
                color = SecondaryBlue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTitle(
    text: String
) {
    Text(
        text = text,
        color = EmergencyRed,
        fontFamily = Salsa,
        fontSize = 20.sp
    )
}

@Composable
private fun ProfileField(
    label: String,
    value: String
) {
    if (value.isNotBlank()) {

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = label,
            color = EmergencyRed,
            fontFamily = Salsa,
            fontSize = 16.sp
        )

        Text(
            text = value,
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 24.dp)
        )
    }
}