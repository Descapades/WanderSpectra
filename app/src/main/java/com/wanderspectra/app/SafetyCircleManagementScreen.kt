package com.wanderspectra.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun SafetyCircleManagementScreen(
    caregiverId: String,
    childId: String,
    repository: SafetyCircleRepository = remember {
        SafetyCircleRepository()
    },
    onBackHandlerChanged: (() -> Unit) -> Unit,
    onBackAvailabilityChanged: (Boolean) -> Unit
) {
    key(caregiverId, childId) {
        var members by remember { mutableStateOf<List<SafetyCircleMember>>(emptyList()) }
        var loading by remember { mutableStateOf(true) }
        var busy by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var removal by remember { mutableStateOf<SafetyCircleMember?>(null) }
        var formMember by remember { mutableStateOf<SafetyCircleMember?>(null) }
        var viewingMember by remember { mutableStateOf<SafetyCircleMember?>(null) }
        val available = caregiverId.isNotBlank() && childId.isNotBlank()
        var disposed by remember { mutableStateOf(false) }

        SideEffect {
            onBackAvailabilityChanged(
                viewingMember != null || formMember != null || removal != null
            )

            onBackHandlerChanged {
                when {
                    busy -> Unit
                    removal != null -> removal = null
                    formMember != null -> { formMember = null; error = null }
                    viewingMember != null -> viewingMember = null
                }
            }
        }

        DisposableEffect(Unit) { onDispose { disposed = true } }
        fun load() {
            if (!available || busy) return
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
                                            error = caregiverDataError(exception)
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
                        error = caregiverDataError(it); loading = false
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
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (formMember == null && viewingMember == null) {
                    error?.let { message ->
                        DataStateFeedback(
                            title = "Unable to load Safety Circle",
                            message = message,
                            onRetry = if (!busy && !loading) ({ load() }) else null
                        )
                    }
                }
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    val selectedMember = formMember
                    val selectedViewingMember = viewingMember

                    if (selectedViewingMember != null) {
                        SafetyCircleMemberDetailsScreen(
                            member = selectedViewingMember,
                            onBack = {
                                viewingMember = null
                            },
                            onEdit = {
                                formMember = selectedViewingMember
                                viewingMember = null
                            },
                            onRemove = {
                                removal = selectedViewingMember
                            }
                        )
                    } else if (selectedMember != null) {
                        SafetyCircleMemberFormScreen(
                            member = selectedMember,
                            saveError = error,
                            isSaving = busy,
                            onBack = {
                                if (!busy) {
                                    formMember = null
                                    error = null
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
                                            if (!disposed) {
                                                busy = false
                                                formMember = null
                                                load()
                                            }
                                        },
                                        onError = { exception ->
                                            if (!disposed) {
                                                busy = false
                                                error = caregiverDataError(exception)
                                            }
                                        }
                                    )

                                }
                            }
                        )
                    } else if (loading) {
                        DataStateFeedback(
                            title = "Safety Circle",
                            message = "Loading members...",
                            loading = true
                        )
                    } else if (error == null) {
                        SafetyCircleContent(
                            members = members.map { member ->
                                SafetyCircleMemberUiModel(
                                    memberId = member.memberId,
                                    name = member.name,
                                    relationship = member.relationship,
                                    phone = member.phone,
                                    isActive = member.isActive,
                                    elopementAlertsEnabled = member.elopementAlertsEnabled,
                                    locationAccess = member.locationAccess
                                )
                            },
                            actionsEnabled = !busy && !loading,
                            onAddMember = {
                                if (!busy && !loading) {
                                    error = null
                                    formMember = SafetyCircleMember(
                                        caregiverId = caregiverId,
                                        childId = childId
                                    )
                                }
                            },
                            onViewMember = { ui ->
                                if (!busy && !loading) {
                                    error = null
                                    viewingMember = members.find { it.memberId == ui.memberId }
                                }
                            }
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
                            Text("Permanently remove ${member.name} and all of their child assignments? To remove only one assignment, use Edit Member.")
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
                                        if (viewingMember?.memberId == member.memberId) {
                                            viewingMember = null
                                        }
                                        busy = false
                                        removal = null
                                    }
                                },
                                onError = {
                                    if (!disposed) {
                                        busy = false; error =
                                            caregiverDataError(it)
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
