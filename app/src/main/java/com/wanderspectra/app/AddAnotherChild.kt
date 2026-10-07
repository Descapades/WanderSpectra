package com.wanderspectra.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.BackgroundCream
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import com.wanderspectra.app.ui.theme.ButtonYellow
import com.wanderspectra.app.ui.theme.TanSongbird
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun AddAnotherChild(
    onChildProfileCreated: (ChildProfile) -> Unit = {}
) {
    var childName by remember { mutableStateOf("") }
    var preferredName by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var hairColor by remember { mutableStateOf("") }
    var eyeColor by remember { mutableStateOf("") }

    var allergies by remember { mutableStateOf("") }
    var medicalConsiderations by remember { mutableStateOf("") }
    var criticalInformation by remember { mutableStateOf("") }

    var communicationStyle by remember { mutableStateOf("") }
    var helpfulCommunicationInformation by remember { mutableStateOf("") }

    var sensorySensitivities by remember { mutableStateOf("") }
    var elopementTriggers by remember { mutableStateOf("") }
    var calmingStrategies by remember { mutableStateOf("") }
    var safetyConcerns by remember { mutableStateOf("") }
    var safetyPrompt by remember { mutableStateOf("") }

    var validationMessage by remember {
        mutableStateOf<String?>(null)
    }

    var selectedPhotoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val childProfileRepository = remember {
        ChildProfileRepository()
    }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                selectedPhotoUri = uri
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {

        // WanderSpectra Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 32.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Wander",
                color = EmergencyRed,
                fontFamily = TanSongbird,
                fontSize = 22.sp
            )

            Text(
                text = "Spectra",
                color = PrimaryBlue,
                fontFamily = TanSongbird,
                fontSize = 22.sp
            )

            Image(
                painter = painterResource(R.drawable.wanderspectra_logo),
                contentDescription = "WanderSpectra Logo",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(60.dp)
            )
        }

        // Outer yellow card
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    start = 28.dp,
                    end = 28.dp,
                    top = 95.dp,
                    bottom = 24.dp
                )
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = PrimaryBlue,
                    shape = RoundedCornerShape(14.dp)
                )
                .background(
                    color = ButtonYellow,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(14.dp)
        ) {

            // Inner scrollable cream card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = BackgroundCream,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    text = "Add Another Child",
                    color = EmergencyRed,
                    fontFamily = TanSongbird,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                    value = childName,
                    onValueChange = { childName = it }
                )

                OnboardingField(
                    label = "Preferred Name",
                    placeholder = "Name",
                    value = preferredName,
                    onValueChange = { preferredName = it }
                )

                OnboardingField(
                    label = "Enter Child Birthdate",
                    placeholder = "MM/DD/YYYY",
                    value = birthdate,
                    onValueChange = { birthdate = it },
                    keyboardType = KeyboardType.Number
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
                        .width(295.dp)
                        .height(340.dp)
                        .border(
                            width = 1.dp,
                            color = PrimaryBlue,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .background(
                            color = CardCream,
                            shape = RoundedCornerShape(10.dp)
                        )
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
                            contentDescription = "Selected child profile picture",
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
                    onValueChange = { height = it },
                    keyboardType = KeyboardType.Decimal
                )

                OnboardingField(
                    label = "Weight",
                    placeholder = "Weight",
                    value = weight,
                    onValueChange = { weight = it },
                    keyboardType = KeyboardType.Decimal
                )

                OnboardingField(
                    label = "Hair Color",
                    placeholder = "Hair Color",
                    value = hairColor,
                    onValueChange = { hairColor = it }
                )

                OnboardingField(
                    label = "Eye Color",
                    placeholder = "Eye Color",
                    value = eyeColor,
                    onValueChange = { eyeColor = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Emergency Information",
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OnboardingField(
                    label = "Allergies",
                    placeholder = "Allergies",
                    value = allergies,
                    onValueChange = { allergies = it }
                )

                OnboardingField(
                    label = "Medical Considerations",
                    placeholder = "Medical Considerations",
                    value = medicalConsiderations,
                    onValueChange = { medicalConsiderations = it }
                )

                OnboardingField(
                    label = "Critical Information",
                    placeholder = "Critical Information",
                    value = criticalInformation,
                    onValueChange = { criticalInformation = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Communication",
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OnboardingField(
                    label = "Communication Style",
                    placeholder = "nonverbal, minimally verbal, verbal",
                    value = communicationStyle,
                    onValueChange = { communicationStyle = it }
                )

                OnboardingField(
                    label = "Helpful Information",
                    placeholder = "Helpful Communication Information",
                    value = helpfulCommunicationInformation,
                    onValueChange = {
                        helpfulCommunicationInformation = it
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Safety & Sensory Info",
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OnboardingField(
                    label = "Sensory Sensitivities",
                    placeholder = "Sensory Sensitivities",
                    value = sensorySensitivities,
                    onValueChange = { sensorySensitivities = it }
                )

                OnboardingField(
                    label = "Elopement Triggers",
                    placeholder = "Elopement Triggers",
                    value = elopementTriggers,
                    onValueChange = { elopementTriggers = it }
                )

                OnboardingField(
                    label = "Calming Strategies",
                    placeholder = "Calming Strategies",
                    value = calmingStrategies,
                    onValueChange = { calmingStrategies = it }
                )

                OnboardingField(
                    label = "Safety Concerns",
                    placeholder = "Safety Concerns",
                    value = safetyConcerns,
                    onValueChange = { safetyConcerns = it }
                )

                OnboardingField(
                    label = "Safety Prompt",
                    placeholder = "Safety message displayed on watch",
                    value = safetyPrompt,
                    onValueChange = { safetyPrompt = it }
                )

                validationMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = message,
                        fontFamily = Salsa,
                        fontSize = 12.sp,
                        color = EmergencyRed,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {

                        when {

                            childName.isBlank() -> {
                                validationMessage =
                                    "Please enter the child's name."
                            }

                            birthdate.isBlank() -> {
                                validationMessage =
                                    "Please enter the child's birthdate."
                            }

                            communicationStyle.isBlank() -> {
                                validationMessage =
                                    "Please enter the child's communication style."
                            }

                            else -> {

                                validationMessage = null

                                val childProfile = ChildProfile(
                                    fullName = childName.trim(),
                                    preferredName = preferredName.trim(),
                                    birthdate = birthdate.trim(),
                                    height = height.trim(),
                                    weight = weight.trim(),
                                    hairColor = hairColor.trim(),
                                    eyeColor = eyeColor.trim(),

                                    communicationStyle =
                                        communicationStyle.trim(),

                                    helpfulCommunicationInfo =
                                        helpfulCommunicationInformation.trim(),

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

                                childProfileRepository.createChildProfile(
                                    childProfile = childProfile,

                                    onSuccess = { _ ->
                                        onChildProfileCreated(childProfile)
                                    },

                                    onFailure = {
                                        validationMessage =
                                            "Child profile could not be saved. Please try again."
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(
                            width = 1.dp,
                            color = PrimaryBlue,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonBlue,
                        contentColor = SecondaryBlue
                    )
                ) {
                    Text(
                        text = "Save Child Profile",
                        fontFamily = Salsa,
                        fontSize = 16.sp,
                        color = SecondaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

}