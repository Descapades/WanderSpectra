package com.wanderspectra.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Repository adapter; the existing SafetyCircleContent remains reusable. */
@Composable
fun SafetyCircleManagementScreen(
    caregiverId: String,
    childId: String,
    repository: SafetyCircleRepository = remember {
        SafetyCircleRepository()
    },
    onBack: () -> Unit = {}
) {
    key(caregiverId, childId) {
        var members by remember { mutableStateOf<List<SafetyCircleMember>>(emptyList()) }
        var loading by remember { mutableStateOf(false) }
        var busy by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var removal by remember { mutableStateOf<SafetyCircleMember?>(null) }
        var formMember by remember { mutableStateOf<SafetyCircleMember?>(null) }
        val available = caregiverId.isNotBlank() && childId.isNotBlank()
        var disposed by remember { mutableStateOf(false) }
        DisposableEffect(Unit) { onDispose { disposed = true } }
        fun load() {
            if (!available) return
            loading = true
            error = null
            repository.getMembersForCaregiver(
                caregiverId,

                onSuccess = { allMembers ->
                    if (!disposed) {
                        if (allMembers.isEmpty()) {
                            members = emptyList()
                            loading = false
                        } else {
                            val matchingMembers = mutableListOf<SafetyCircleMember>()
                            var remaining = allMembers.size
                            var failed = false

                            allMembers.forEach { member ->
                                repository.isMemberAssignedToChild(
                                    member = member,
                                    childId = childId,
                                    onSuccess = { isAssigned ->
                                        if (!disposed && !failed) {
                                            if (isAssigned) {
                                                matchingMembers.add(member)
                                            }

                                            remaining--

                                            if (remaining == 0) {
                                                members = matchingMembers.sortedBy {
                                                    it.name.lowercase()
                                                }
                                                loading = false
                                            }
                                        }
                                    },
                                    onError = { exception ->
                                        if (!disposed && !failed) {
                                            failed = true
                                            error = exception.localizedMessage
                                                ?: "Unable to load child assignments."
                                            loading = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                },

                onError = {
                    if (!disposed) {
                        error = it.localizedMessage ?: "Unable to load members."; loading = false
                    }
                })
        }
        LaunchedEffect(caregiverId, childId) { load() }
        Column(Modifier.fillMaxSize()) {
            if (!available) {
                Text(
                    "Sign in and load a child profile to manage the Safety Circle.",
                    Modifier.padding(16.dp)
                )
            } else {
                if (loading || busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                error?.let { message ->
                    Text(message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
                    TextButton(
                        onClick = { load() },
                        enabled = !busy && !loading
                    ) { Text("Retry loading") }
                }
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    val selectedMember = formMember

                    if (selectedMember != null) {
                        SafetyCircleMemberFormScreen(
                            member = selectedMember,
                            saveError = error,
                            isSaving = busy,
                            onBack = {
                                if (!busy) {
                                    formMember = null
                                }
                            },
                            onSave = { updatedMember, selectedChildIds ->
                                if (!busy && selectedChildIds.isNotEmpty()) {
                                    val memberToSave = updatedMember.copy(
                                        caregiverId = caregiverId,
                                        childId = if (updatedMember.memberId.isBlank()) {
                                            selectedChildIds.first()
                                        } else {
                                            updatedMember.childId
                                        }
                                    )

                                    busy = true
                                    error = null

                                    repository.saveMemberWithAssignments(
                                        member = memberToSave,
                                        selectedChildIds = selectedChildIds,
                                        onSuccess = {
                                            busy = false
                                            formMember = null
                                            load()
                                        },
                                        onError = { exception ->
                                            busy = false
                                            error = exception.message
                                                ?: "Failed to save Safety Circle member."
                                        }
                                    )

                                }
                            }
                        )
                    } else {
                        SafetyCircleContent(
                            members = members.map { member ->
                                SafetyCircleMemberUiModel(
                                    memberId = member.memberId,
                                    name = member.name,
                                    relationship = member.relationship,
                                    contactInformation = member.contactInformation,
                                    isActive = member.isActive,
                                    elopementAlertsEnabled = member.elopementAlertsEnabled,
                                    locationAccess = member.locationAccess
                                )
                            },
                            onAddMember = {
                                if (!busy && !loading) {
                                    error = null
                                    formMember = SafetyCircleMember(
                                        caregiverId = caregiverId,
                                        childId = childId
                                    )
                                }
                            },
                            onEditMember = { ui ->
                                if (!busy && !loading) {
                                    error = null
                                    formMember = members.find { it.memberId == ui.memberId }
                                }
                            },
                            onRemoveMember = { ui ->
                                if (!busy && !loading) removal =
                                    members.find { it.memberId == ui.memberId }
                            },
                            onViewMember = { ui ->
                                if (!busy && !loading) {
                                    error = null
                                    formMember = members.find { it.memberId == ui.memberId }
                                }
                            },
                            onBack = onBack
                        )
                    }
                }
            }

            removal?.let { member ->
                AlertDialog(
                    onDismissRequest = { if (!busy) removal = null },
                    title = { Text("Remove member?") },
                    text = {
                        Column {
                            Text("Remove ${member.name} from this child's Safety Circle?")
                            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    },
                    confirmButton = {
                        TextButton(enabled = !busy, onClick = {
                            busy = true
                            error = null
                            repository.removeMember(
                                member.memberId,
                                onSuccess = {
                                    if (!disposed) {
                                        members =
                                            members.filterNot { it.memberId == member.memberId }
                                        busy = false
                                        removal = null
                                    }
                                },
                                onError = {
                                    if (!disposed) {
                                        busy = false; error =
                                            it.localizedMessage ?: "Unable to remove member."
                                    }
                                })
                        }) { Text(if (busy) "Removing..." else "Remove") }
                    },
                    dismissButton = {
                        TextButton(
                            enabled = !busy,
                            onClick = { removal = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}
