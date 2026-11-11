package com.wanderspectra.app


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import com.wanderspectra.app.ui.theme.TanSongbird

@Composable
fun SafetyCircleMemberFormScreen(
    member: SafetyCircleMember,
    saveError: String?,
    onBack: () -> Unit,
    onSave: (SafetyCircleMember, Set<String>) -> Unit,
    isSaving: Boolean,
) {
    var children by remember { mutableStateOf<List<ChildProfile>>(emptyList()) }
    var selectedChildIds by remember(member.memberId) { mutableStateOf(member.childId.takeIf { it.isNotBlank() }?.let { setOf(it) } ?: emptySet<String>()) }
    var assignmentsLoading by remember(member.memberId) { mutableStateOf(member.memberId.isNotBlank()) }
    var name by remember(member.memberId) { mutableStateOf(member.name) }
    var relationship by remember(member.memberId) { mutableStateOf(member.relationship) }
    var phone by remember(member.memberId) { mutableStateOf(member.phone) }
    var email by remember(member.memberId) { mutableStateOf(member.email) }
    var address by remember(member.memberId) { mutableStateOf(member.address) }
    var isActive by remember(member.memberId) { mutableStateOf(member.isActive) }
    var elopementAlertsEnabled by remember(member.memberId) { mutableStateOf(member.elopementAlertsEnabled) }
    var locationAccess by remember(member.memberId) { mutableStateOf(member.locationAccess) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val childRepository = remember { ChildProfileRepository() }
    val safetyCircleRepository = remember { SafetyCircleRepository() }

    LaunchedEffect(member.memberId) {
        if (member.memberId.isNotBlank()) {
            safetyCircleRepository.getAssignedChildIds(
                memberId = member.memberId,
                onSuccess = { assignedIds ->
                    selectedChildIds = assignedIds.ifEmpty {
                        member.childId.takeIf { it.isNotBlank() }
                            ?.let { setOf(it) }
                            ?: emptySet()
                    }
                    assignmentsLoading = false
                },
                onError = { exception ->
                    errorMessage = exception.localizedMessage
                        ?: "Unable to load assigned children."
                    assignmentsLoading = false
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        childRepository.getChildProfiles(
            onSuccess = { children = it },
            onFailure = {
                errorMessage = it.localizedMessage
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Add Member",
            color = EmergencyRed,
            fontFamily = TanSongbird,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        OnboardingField(
            label = "Member Name",
            placeholder = "Name",
            value = name,
            onValueChange = { name = it }
        )

        OnboardingField(
            label = "Relationship",
            placeholder = "Relationship",
            value = relationship,
            onValueChange = { relationship = it }
        )

        OnboardingField(
            label = "Phone",
            placeholder = "Phone Number",
            value = phone,
            onValueChange = { phone = it }
        )

        OnboardingField(
            label = "Email",
            placeholder = "Email Address",
            value = email,
            onValueChange = { email = it }
        )

        OnboardingField(
            label = "Address",
            placeholder = "Street Address",
            value = address,
            onValueChange = { address = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = EmergencyRed)) {
                        append("Status: ")
                    }
                    withStyle(SpanStyle(color = PrimaryBlue)) {
                        append(if (isActive) "Active" else "Inactive")
                    }
                },
                fontFamily = Salsa,
                fontSize = 20.sp,
            )

            Switch(
                checked = isActive,
                onCheckedChange = { isActive = it }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = EmergencyRed)) {
                        append("Elopement Alerts: ")
                    }
                    withStyle(SpanStyle(color = PrimaryBlue)) {
                        append(if (elopementAlertsEnabled) "On" else "Off")
                    }
                },
                fontFamily = Salsa,
                fontSize = 20.sp,
            )

            Switch(
                checked = elopementAlertsEnabled,
                onCheckedChange = { elopementAlertsEnabled = it }
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Location Access:",
            fontFamily = Salsa,
            fontSize = 20.sp,
            color = EmergencyRed
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = locationAccess == "ALWAYS",
                onClick = { locationAccess = "ALWAYS" }
            )

            Text(
                text = "Always",
                fontFamily = Salsa,
                fontSize = 20.sp,
                color = PrimaryBlue
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = locationAccess == "DURING_ELOPEMENT",
                onClick = { locationAccess = "DURING_ELOPEMENT" }
            )

            Text(
                text = "During Elopement",
                fontFamily = Salsa,
                fontSize = 20.sp,
                color = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = "Assigned Children:",
            fontFamily = Salsa,
            fontSize = 20.sp,
            color = EmergencyRed
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            children.forEach { child ->
                val childName = child.preferredName.ifBlank {
                    child.fullName
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = childName,
                        color = PrimaryBlue,
                        fontFamily = Salsa,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 20.dp)
                    )

                    Switch(
                        checked = child.childId in selectedChildIds,
                        onCheckedChange = { enabled ->
                            selectedChildIds = if (enabled) {
                                selectedChildIds + child.childId
                            } else {
                                selectedChildIds - child.childId
                            }
                        }
                    )
                }
            }
        }


        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        saveError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSave(
                    member.copy(
                        name = name.trim(),
                        relationship = relationship.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        address = address.trim(),
                        isActive = isActive,
                        elopementAlertsEnabled = elopementAlertsEnabled,
                        locationAccess = locationAccess
                    ),
                    selectedChildIds
                )
            },
            enabled = name.isNotBlank() &&
                    relationship.isNotBlank() &&
                    phone.isNotBlank() &&
                    selectedChildIds.isNotEmpty() &&
                    !assignmentsLoading &&
                    errorMessage == null &&
                    !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue,
                contentColor = SecondaryBlue,
                disabledContainerColor = ButtonBlue,
                disabledContentColor = SecondaryBlue
            ),
            border = BorderStroke(1.dp, SecondaryBlue)
        ) {
            Text(
                text = "Save",
                fontFamily = Salsa,
                fontSize = 18.sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonRed,
                contentColor = SecondaryRed
            ),
            border = BorderStroke(1.dp, SecondaryRed)
        ) {
            Text(
                text = "Back",
                fontFamily = Salsa,
                fontSize = 18.sp,
                maxLines = 1
            )
        }
    }
}
