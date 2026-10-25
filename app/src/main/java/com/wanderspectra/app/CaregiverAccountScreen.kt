package com.wanderspectra.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.TanSongbird
import com.google.firebase.auth.EmailAuthProvider


@Composable
fun CaregiverAccountContent() {

    var caregiver by remember {
        mutableStateOf<Map<String, Any>?>(null)
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

    var isSaving by remember {
        mutableStateOf(false)
    }

    var saveError by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            errorMessage = "No signed-in caregiver found."
            isLoading = false
        } else {
            FirebaseFirestore.getInstance()
                .collection("caregivers")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    caregiver = if (document.exists()) {
                        document.data
                    } else {
                        null
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    errorMessage = exception.message
                    isLoading = false
                }
        }
    }

    if (isEditing && caregiver != null) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (saveError != null) {
                Text(
                    text = saveError ?: "",
                    color = EmergencyRed,
                    fontFamily = Salsa,
                    modifier = Modifier.padding(12.dp)
                )
            }

            EditCaregiverProfile(
                caregiver = caregiver!!,
                isSaving = isSaving,
                onBack = {
                    if (!isSaving) {
                        saveError = null
                        isEditing = false
                    }
                },
                onSave = { phone, location, relationship,
                           currentPassword, newPassword ->

                    val user = FirebaseAuth.getInstance().currentUser

                    if (user == null) {
                        saveError = "You must be signed in."
                    } else if (newPassword.isNotBlank() && currentPassword.isBlank()) {
                        saveError = "Enter your current password."
                    } else if (newPassword.isNotBlank() && newPassword.length < 6) {
                        saveError = "Your new password must contain at least 6 characters."
                    } else {
                        isSaving = true
                        saveError = null

                        val updates = mapOf(
                            "phone" to phone,
                            "location" to location,
                            "relationship" to relationship
                        )

                        fun saveProfile() {
                            FirebaseFirestore.getInstance()
                                .collection("caregivers")
                                .document(user.uid)
                                .update(updates)
                                .addOnSuccessListener {
                                    caregiver = caregiver!!.toMutableMap().apply {
                                        putAll(updates)
                                    }
                                    isSaving = false
                                    isEditing = false
                                }
                                .addOnFailureListener { exception ->
                                    saveError = exception.message
                                        ?: "Unable to save caregiver profile."
                                    isSaving = false
                                }
                        }

                        if (newPassword.isBlank()) {
                            saveProfile()
                        } else {
                            val userEmail = user.email

                            if (userEmail.isNullOrBlank()) {
                                saveError = "This account does not have an email address."
                                isSaving = false
                            } else {
                                val credential = EmailAuthProvider.getCredential(
                                    userEmail,
                                    currentPassword
                                )

                                user.reauthenticate(credential)
                                    .addOnSuccessListener {
                                        user.updatePassword(newPassword)
                                            .addOnSuccessListener {
                                                saveProfile()
                                            }
                                            .addOnFailureListener { exception ->
                                                saveError = exception.message
                                                    ?: "Unable to update password."
                                                isSaving = false
                                            }
                                    }
                                    .addOnFailureListener {
                                        saveError =
                                            "Unable to verify your current password. Please check it and try again."
                                        isSaving = false
                                    }
                            }
                        }
                    }
                }
            )
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(color = PrimaryBlue)
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Unable to load caregiver.",
                    color = PrimaryBlue,
                    fontFamily = Salsa
                )
            }

            caregiver == null -> {
                Text(
                    text = "No caregiver profile found.",
                    color = PrimaryBlue,
                    fontFamily = Salsa
                )
            }

            else -> {
                val data = caregiver!!

                val firstName = data["firstName"] as? String ?: ""
                val lastName = data["lastName"] as? String ?: ""
                val phone = data["phone"] as? String ?: ""
                val location = data["location"] as? String ?: ""
                val email = data["email"] as? String ?: ""
                val relationship = data["relationship"] as? String ?: ""

                val fullName = "$firstName $lastName".trim()

                Text(
                    text = fullName.ifBlank { "Caregiver Name" },
                    color = EmergencyRed,
                    fontFamily = TanSongbird,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                CaregiverField("Telephone:", phone)
                CaregiverField("Address:", location)
                CaregiverField("Email:", email)
                CaregiverField("Relationship to Child:", relationship)

                Spacer(modifier = Modifier.height(90.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            isEditing = true
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonBlue
                        ),
                        border = BorderStroke(1.dp, SecondaryBlue)
                    ) {
                        Text(
                            text = "Edit Profile",
                            fontFamily = Salsa,
                            fontSize = 16.sp,
                            color = SecondaryBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaregiverField(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = EmergencyRed,
            fontFamily = Salsa,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = value.ifBlank { "Not provided" },
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 16.sp
        )
    }
}