package com.wanderspectra.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.BackgroundCream
import com.wanderspectra.app.ui.theme.ButtonYellow
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.TanSongbird
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.SecondaryRed
import androidx.compose.foundation.layout.PaddingValues
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.text.style.TextAlign

@Composable
fun OnboardingScreen(
    onAccountCreated: () -> Unit
) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var childName by remember { mutableStateOf("") }
    var preferredName by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
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
    var confirmPassword by remember { mutableStateOf("") }
    var registrationMessage by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    val caregiverRepository = remember {
        CaregiverRepository()
    }

    fun registerCaregiver() {
        registrationMessage = null

        when {
            email.isBlank() -> {
                registrationMessage = "Please enter an email address."
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> {
                registrationMessage = "Please enter a valid email address."
            }

            password.isBlank() -> {
                registrationMessage = "Please enter a password."
            }

            password.length < 6 -> {
                registrationMessage = "Password must be at least 6 characters."
            }

            password != confirmPassword -> {
                registrationMessage = "Passwords do not match."
            }

            else -> {
                isRegistering = true

                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            val caregiver = Caregiver(
                                username = username.trim(),
                                email = email.trim(),
                                phone = phone.trim(),
                                location = location.trim(),
                                relationship = relationship.trim()
                            )

                            caregiverRepository.createCaregiver(
                                caregiver = caregiver,
                                onSuccess = {
                                    isRegistering = false
                                    registrationMessage =
                                        "Account created successfully."
                                    onAccountCreated()
                                },
                                onFailure = {
                                    isRegistering = false
                                    registrationMessage =
                                        "Account created, but caregiver information could not be saved."
                                }
                            )

                        } else {
                            isRegistering = false
                            registrationMessage =
                                task.exception?.localizedMessage
                                    ?: "Account registration failed. Please try again."
                        }
                    }
            }
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

        // Outer yellow onboarding card
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

            // Inner scrollable card
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
                    text = "Caregiver Information",
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OnboardingField(
                    label = "Enter Username",
                    placeholder = "Username",
                    value = username,
                    onValueChange = { username = it }
                )

                OnboardingField(
                    label = "Enter Password",
                    placeholder = "Password",
                    value = password,
                    onValueChange = { password = it },
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )

                OnboardingField(
                    label = "Confirm Password",
                    placeholder = "Confirm Password",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )

                OnboardingField(
                    label = "Enter Valid Email Address",
                    placeholder = "Email",
                    value = email,
                    onValueChange = { email = it },
                    keyboardType = KeyboardType.Email
                )

                OnboardingField(
                    label = "Enter Phone Number",
                    placeholder = "+1 (   )",
                    value = phone,
                    onValueChange = { phone = it },
                    keyboardType = KeyboardType.Phone
                )

                OnboardingField(
                    label = "Enter Location",
                    placeholder = "Address, City, State",
                    value = location,
                    onValueChange = { location = it }
                )

                OnboardingField(
                    label = "Relationship to Child",
                    placeholder = "Relationship",
                    value = relationship,
                    onValueChange = { relationship = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                        .fillMaxWidth()
                        .height(110.dp)
                        .border(
                            width = 1.dp,
                            color = PrimaryBlue,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .background(
                            color = CardCream,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add Photo",
                        color = SecondaryBlue,
                        fontFamily = Salsa,
                        fontSize = 14.sp
                    )
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
                    onValueChange = { helpfulCommunicationInformation = it }
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

                registrationMessage?.let { message ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = message,
                        fontFamily = Salsa,
                        fontSize = 12.sp,
                        color = if (message == "Account created successfully.") {
                            SecondaryBlue
                        } else {
                            EmergencyRed
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Button(
                        onClick = {
                            // Add another child functionality will be connected later
                        },
                        modifier = Modifier
                            .weight(1f)
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
                            text = "Add Another Child",
                            fontFamily = Salsa,
                            fontSize = 12.sp,
                            color = SecondaryBlue,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            registerCaregiver()
                        },
                        enabled = !isRegistering,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(
                                width = 1.dp,
                                color = SecondaryRed,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonRed,
                            contentColor = SecondaryRed
                        )
                    ) {
                        Text(
                            text = "Create Account",
                            fontFamily = Salsa,
                            fontSize = 12.sp,
                            color = SecondaryRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

            }
        }
    }
}

@Composable
fun OnboardingField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {

    Text(
        text = label,
        color = SecondaryBlue,
        fontFamily = Salsa,
        fontSize = 11.sp
    )

    Spacer(modifier = Modifier.height(5.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = Salsa,
                fontSize = 12.sp
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        visualTransformation =
            if (isPassword) {
                PasswordVisualTransformation()
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
            },
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = PrimaryBlue,
            focusedContainerColor = CardCream,
            unfocusedContainerColor = CardCream
        )
    )

    Spacer(modifier = Modifier.height(12.dp))
}



