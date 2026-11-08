
package com.wanderspectra.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
fun SafetyCircleMemberDetailsScreen(
    member: SafetyCircleMember,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    val repository = remember { SafetyCircleRepository() }
    val childRepository = remember { ChildProfileRepository() }
    var childProfiles by remember { mutableStateOf<List<ChildProfile>>(emptyList()) }
    var assignedChildIds by remember(member.memberId) { mutableStateOf<Set<String>>(emptySet()) }
    var assignmentsLoading by remember(member.memberId) { mutableStateOf(true) }
    var assignmentsError by remember(member.memberId) { mutableStateOf<String?>(null) }

    LaunchedEffect(member.memberId) {
        assignmentsLoading = true
        assignmentsError = null

        repository.getAssignedChildIds(
            memberId = member.memberId,
            onSuccess = { childIds ->
                assignedChildIds = childIds
                assignmentsLoading = false
            },
            onError = { exception ->
                assignmentsError = exception.localizedMessage
                    ?: "Unable to load assigned children."
                assignmentsLoading = false
            }
        )
    }

    LaunchedEffect(member.memberId) {
        childRepository.getChildProfiles(
            onSuccess = { children ->
                childProfiles = children
            },
            onFailure = { exception ->
                assignmentsError = exception.localizedMessage
                    ?: "Unable to load child profiles."
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Safety Circle Member",
            color = EmergencyRed,
            fontFamily = TanSongbird,
            fontSize = 20.sp,
            lineHeight = 30.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = member.name,
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append("Relationship: ")
                }
                withStyle(SpanStyle(color = PrimaryBlue)) {
                    append(member.relationship)
                }
            },
            fontFamily = Salsa,
            fontSize = 20.sp
        )

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append("Phone: ")
                }
                withStyle(SpanStyle(color = PrimaryBlue)) {
                    append(member.phone)
                }
            },
            fontFamily = Salsa,
            fontSize = 20.sp
        )

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append("Email: ")
                }
                withStyle(SpanStyle(color = PrimaryBlue)) {
                    append(member.email)
                }
            },
            fontFamily = Salsa,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append("Address: ")
                }
                withStyle(SpanStyle(color = PrimaryBlue)) {
                    append(member.address)
                }
            },
            fontFamily = Salsa,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append("Status: ")
                }
                withStyle(SpanStyle(color = PrimaryBlue)) {
                    append(if (member.isActive) "Active" else "Inactive")
                }
            },
            fontFamily = Salsa,
            fontSize = 20.sp
        )

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append("Elopement Alerts: ")
                }
                withStyle(SpanStyle(color = PrimaryBlue)) {
                    append(if (member.elopementAlertsEnabled) "On" else "Off")
                }
            },
            fontFamily = Salsa,
            fontSize = 20.sp
        )

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = EmergencyRed)) {
                    append("Location Access: ")
                }
                withStyle(SpanStyle(color = PrimaryBlue)) {
                    append(
                        when (member.locationAccess) {
                            "ALWAYS" -> "Always"
                            "DURING_ELOPEMENT" -> "During Elopement"
                            "NEVER" -> "Never"
                            else -> member.locationAccess
                        }
                    )
                }
            },
            fontFamily = Salsa,
            fontSize = 20.sp
        )

        Text(
            text = "Assigned Children:",
            color = EmergencyRed,
            fontFamily = Salsa,
            fontSize = 20.sp
        )

        when {
            assignmentsLoading -> {
                CircularProgressIndicator()
            }

            assignmentsError != null -> {
                Text(
                    text = assignmentsError ?: "",
                    color = EmergencyRed,
                    fontFamily = Salsa,
                    fontSize = 20.sp
                )
            }

            assignedChildIds.isEmpty() -> {
                Text(
                    text = "No children assigned",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 20.sp
                )
            }

            else -> {
                childProfiles
                    .filter { it.childId in assignedChildIds }
                    .sortedBy { it.fullName.lowercase() }
                    .forEach { child ->
                        Text(
                            text = child.fullName,
                            color = PrimaryBlue,
                            fontFamily = Salsa,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(start = 20.dp, bottom = 4.dp)
                        )
                    }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue,
                contentColor = SecondaryBlue
            ),
            border = BorderStroke(1.dp, SecondaryBlue)
        ) {
            Text(
                text = "Edit Member",
                fontFamily = Salsa,
                fontSize = 18.sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRemove,
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
                text = "Remove Member",
                fontFamily = Salsa,
                fontSize = 18.sp,
                maxLines = 1
            )
        }
    }
}


