package com.wanderspectra.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
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
private fun EditChildProfile(
    child: ChildProfile,
    onCancel: () -> Unit,
    onSave: (ChildProfile) -> Unit
) {

    var fullName by remember(child.childId) {
        mutableStateOf(child.fullName)
    }

    var preferredName by remember(child.childId) {
        mutableStateOf(child.preferredName)
    }

    var birthdate by remember(child.childId) {
        mutableStateOf(child.birthdate)
    }

    var height by remember(child.childId) {
        mutableStateOf(child.height)
    }

    var weight by remember(child.childId) {
        mutableStateOf(child.weight)
    }

    var hairColor by remember(child.childId) {
        mutableStateOf(child.hairColor)
    }

    var eyeColor by remember(child.childId) {
        mutableStateOf(child.eyeColor)
    }

    var allergies by remember(child.childId) {
        mutableStateOf(child.allergies)
    }

    var medicalConsiderations by remember(child.childId) {
        mutableStateOf(child.medicalConsiderations)
    }

    var criticalInformation by remember(child.childId) {
        mutableStateOf(child.criticalInformation)
    }

    var communicationStyle by remember(child.childId) {
        mutableStateOf(child.communicationStyle)
    }

    var helpfulCommunicationInfo by remember(child.childId) {
        mutableStateOf(child.helpfulCommunicationInfo)
    }

    var sensorySensitivities by remember(child.childId) {
        mutableStateOf(child.sensorySensitivities)
    }

    var elopementTriggers by remember(child.childId) {
        mutableStateOf(child.elopementTriggers)
    }

    var calmingStrategies by remember(child.childId) {
        mutableStateOf(child.calmingStrategies)
    }

    var safetyConcerns by remember(child.childId) {
        mutableStateOf(child.safetyConcerns)
    }

    var safetyPrompt by remember(child.childId) {
        mutableStateOf(child.safetyPrompt)
    }

    var photoUrl by remember(child.childId) {
        mutableStateOf(child.photoUrl)
    }

    var selectedPhotoUri by remember(child.childId) {
        mutableStateOf<Uri?>(
            child.photoUrl
                .takeIf { it.isNotBlank() }
                ?.let { Uri.parse(it) }
        )
    }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                selectedPhotoUri = uri
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "Child Information",
            color = SecondaryBlue,
            fontFamily = Salsa,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        OnboardingField(
            label = "Enter Child Name",
            placeholder = "Name",
            value = fullName,
            onValueChange = {
                fullName = it
            }
        )

        OnboardingField(
            label = "Preferred Name",
            placeholder = "Name",
            value = preferredName,
            onValueChange = {
                preferredName = it
            }
        )

        OnboardingField(
            label = "Enter Child Birthdate",
            placeholder = "MM/DD/YYYY",
            value = birthdate,
            onValueChange = {
                birthdate = it
            }
        )

        Text(
            text = "Upload Child Profile Picture",
            color = SecondaryBlue,
            fontFamily = Salsa,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (selectedPhotoUri != null) {
                AsyncImage(
                    model = selectedPhotoUri,
                    contentDescription = "Child profile picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "Add Photo",
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OnboardingField(
            label = "Height",
            placeholder = "Height",
            value = height,
            onValueChange = {
                height = it
            }
        )

        OnboardingField(
            label = "Weight",
            placeholder = "Weight",
            value = weight,
            onValueChange = {
                weight = it
            }
        )

        OnboardingField(
            label = "Hair Color",
            placeholder = "Hair Color",
            value = hairColor,
            onValueChange = {
                hairColor = it
            }
        )

        OnboardingField(
            label = "Eye Color",
            placeholder = "Eye Color",
            value = eyeColor,
            onValueChange = {
                eyeColor = it
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Emergency Information",
            color = SecondaryBlue,
            fontFamily = Salsa,
            fontSize = 24.sp
        )

        OnboardingField(
            label = "Allergies",
            placeholder = "Allergies",
            value = allergies,
            onValueChange = {
                allergies = it
            }
        )

        OnboardingField(
            label = "Medical Considerations",
            placeholder = "Medical Considerations",
            value = medicalConsiderations,
            onValueChange = {
                medicalConsiderations = it
            }
        )

        OnboardingField(
            label = "Critical Information",
            placeholder = "Critical Information",
            value = criticalInformation,
            onValueChange = {
                criticalInformation = it
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Communication",
            color = SecondaryBlue,
            fontFamily = Salsa,
            fontSize = 24.sp
        )

        OnboardingField(
            label = "Communication Style",
            placeholder = "nonverbal, minimally verbal, verbal",
            value = communicationStyle,
            onValueChange = {
                communicationStyle = it
            }
        )

        OnboardingField(
            label = "Helpful Information",
            placeholder = "Helpful Communication Information",
            value = helpfulCommunicationInfo,
            onValueChange = {
                helpfulCommunicationInfo = it
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Safety & Sensory Info",
            color = SecondaryBlue,
            fontFamily = Salsa,
            fontSize = 24.sp
        )

        OnboardingField(
            label = "Sensory Sensitivities",
            placeholder = "Sensory Sensitivities",
            value = sensorySensitivities,
            onValueChange = {
                sensorySensitivities = it
            }
        )

        OnboardingField(
            label = "Elopement Triggers",
            placeholder = "Elopement Triggers",
            value = elopementTriggers,
            onValueChange = {
                elopementTriggers = it
            }
        )

        OnboardingField(
            label = "Calming Strategies",
            placeholder = "Calming Strategies",
            value = calmingStrategies,
            onValueChange = {
                calmingStrategies = it
            }
        )

        OnboardingField(
            label = "Safety Concerns",
            placeholder = "Safety Concerns",
            value = safetyConcerns,
            onValueChange = {
                safetyConcerns = it
            }
        )

        OnboardingField(
            label = "Safety Prompt",
            placeholder = "Safety message displayed on watch",
            value = safetyPrompt,
            onValueChange = {
                safetyPrompt = it
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonRed,
                    contentColor = SecondaryRed
                )
            ) {
                Text(
                    text = "Back",
                    fontFamily = Salsa,
                    fontSize = 16.sp,
                    color = SecondaryRed
                )
            }

            Button(
                onClick = {
                    val updatedChild = child.copy(
                        fullName = fullName.trim(),
                        preferredName = preferredName.trim(),
                        birthdate = birthdate.trim(),
                        photoUrl = selectedPhotoUri?.toString() ?: child.photoUrl,
                        height = height.trim(),
                        weight = weight.trim(),
                        hairColor = hairColor.trim(),
                        eyeColor = eyeColor.trim(),
                        communicationStyle = communicationStyle.trim(),
                        helpfulCommunicationInfo =
                            helpfulCommunicationInfo.trim(),
                        allergies = allergies.trim(),
                        medicalConsiderations =
                            medicalConsiderations.trim(),
                        criticalInformation =
                            criticalInformation.trim(),
                        sensorySensitivities =
                            sensorySensitivities.trim(),
                        elopementTriggers =
                            elopementTriggers.trim(),
                        calmingStrategies =
                            calmingStrategies.trim(),
                        safetyConcerns =
                            safetyConcerns.trim(),
                        safetyPrompt =
                            safetyPrompt.trim()
                    )

                    onSave(updatedChild)
                },
                enabled = fullName.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBlue,
                    contentColor = SecondaryBlue
                )
            ) {
                Text(
                    text = "Save",
                    fontFamily = Salsa,
                    fontSize = 16.sp,
                    color = SecondaryBlue
                )
            }
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