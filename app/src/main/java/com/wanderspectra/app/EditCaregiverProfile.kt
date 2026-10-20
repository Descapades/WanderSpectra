package com.wanderspectra.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.*

@Composable
fun EditCaregiverProfile(
    caregiver: Map<String, Any>,
    onBack: () -> Unit,
    onSave: (
        phone: String,
        location: String,
        relationship: String,
        currentPassword: String,
        newPassword: String
    ) -> Unit,
    isSaving: Boolean = false
) {
    var phone by remember(caregiver) {
        mutableStateOf(caregiver["phone"] as? String ?: "")
    }
    var location by remember(caregiver) {
        mutableStateOf(caregiver["location"] as? String ?: "")
    }
    var relationship by remember(caregiver) {
        mutableStateOf(caregiver["relationship"] as? String ?: "")
    }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Text(
            text = "Caregiver Information",
            color = SecondaryBlue,
            fontFamily = Salsa,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        CaregiverEditField(
            label = "Enter Phone Number",
            value = phone,
            keyboardType = KeyboardType.Phone
        ) {
            phone = it
        }

        CaregiverEditField("Enter Location", location) {
            location = it
        }

        CaregiverEditField("Relationship to Child", relationship) {
            relationship = it
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Change Password (optional)",
            color = SecondaryBlue,
            fontFamily = Salsa,
            fontSize = 18.sp
        )

        CaregiverEditField(
            label = "Current Password",
            value = currentPassword,
            isPassword = true
        ) {
            currentPassword = it
        }

        CaregiverEditField(
            label = "New Password",
            value = newPassword,
            isPassword = true
        ) {
            newPassword = it
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonRed
                ),
                border = BorderStroke(1.dp, SecondaryRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Back",
                    fontFamily = Salsa,
                    color = SecondaryRed)
            }

            Button(
                onClick = {
                    onSave(
                        phone.trim(),
                        location.trim(),
                        relationship.trim(),
                        currentPassword,
                        newPassword
                    )
                },
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBlue
                ),
                border = BorderStroke(1.dp, PrimaryBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isSaving) "Saving..." else "Save",
                    fontFamily = Salsa,
                    color = SecondaryBlue
                )
            }
        }
    }
}

@Composable
private fun CaregiverEditField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Text(
        text = label,
        color = PrimaryBlue,
        fontFamily = Salsa,
        fontSize = 14.sp
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))
}