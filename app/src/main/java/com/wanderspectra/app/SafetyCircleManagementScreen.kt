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
        var loaded by remember { mutableStateOf(false) }
        var successMessage by remember { mutableStateOf<String?>(null) }
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
                    formMember != null -> formMember = null
                    viewingMember != null -> viewingMember = null
                }
            }
        }

        DisposableEffect(Unit) { onDispose { disposed = true } }
        fun load() {
            if (!available || busy) return
            loading = true
            error = null
<<<<<<< HEAD
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
=======
            successMessage = null
            repository.getMembersForChild(caregiverId, childId,
                onSuccess = { if (!disposed) { members = it; loaded = true; loading = false } },
                onError = { if (!disposed) { error = caregiverDataError(it); loading = false } })
>>>>>>> 6eab637 (SCRUM-59: Add loading, empty, and error feedback with retry)
        }
        LaunchedEffect(caregiverId, childId) { load() }
        Column(Modifier.fillMaxSize()) {
            if (!available) {
                Text(
                    "Sign in and load a child profile to manage the Safety Circle.",
                    Modifier.padding(16.dp)
                )
            } else {
<<<<<<< HEAD
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
                                    phone = member.phone,
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
=======
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                when {
                    loading -> DataStateFeedback(
                        title = "Safety Circle", message = "Loading members...", loading = true
                    )
                    !loaded && error != null -> DataStateFeedback(
                        title = "Unable to load Safety Circle",
                        message = error.orEmpty(), onRetry = { load() }
                    )
                    else -> {
                        successMessage?.let { Text(it, Modifier.padding(12.dp)) }
                        if (editor == null && removal == null) error?.let { message ->
                            DataStateFeedback(
                                title = "Request unsuccessful", message = message,
                                onRetry = if (busy) null else ({ load() })
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            SafetyCircleContent(
                                members = members.map { SafetyCircleMemberUiModel(it.memberId, it.name, it.relationship, it.contactInformation) },
                                onAddMember = {
                                    if (!busy && !loading) {
                                        error = null
                                        successMessage = null
                                        editor = SafetyCircleMember(caregiverId = caregiverId, childId = childId)
                                    }
                                },
                                onEditMember = { ui -> if (!busy && !loading) {
                                    error = null
                                    successMessage = null
                                    editor = members.find { it.memberId == ui.memberId }
                                } },
                                onRemoveMember = { ui -> if (!busy && !loading) {
                                    error = null
                                    successMessage = null
                                    removal = members.find { it.memberId == ui.memberId }
                                } },
                                actionsEnabled = !busy
                            )
                        }
                    }
                }
            }
        }
        editor?.let { member ->
            SafetyCircleMemberEditor(member, busy,
                onDismiss = { if (!busy) editor = null },
                onSave = { updated ->
                    busy = true
                    error = null
                    repository.saveMember(updated,
                        onSuccess = { saved -> if (!disposed) {
                            members = (members.filterNot { it.memberId == saved.memberId } + saved).sortedBy { it.name.lowercase() }
                            busy = false
                            editor = null
                            successMessage = "Safety Circle member saved."
                        } },
                        onError = { if (!disposed) { busy = false; error = caregiverDataError(it) } })
                },
                error = error
            )
        }
        removal?.let { member ->
            AlertDialog(
                onDismissRequest = { if (!busy) removal = null },
                title = { Text("Remove member?") },
                text = { Column {
                    Text("Remove ${member.name} from this child's Safety Circle?")
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                } },
                confirmButton = { TextButton(enabled = !busy, onClick = {
                    busy = true
                    error = null
                    repository.removeMember(member.memberId,
                        onSuccess = { if (!disposed) {
                            members = members.filterNot { it.memberId == member.memberId }
                            busy = false
                            removal = null
                            successMessage = "Safety Circle member removed."
                        } },
                        onError = { if (!disposed) { busy = false; error = caregiverDataError(it) } })
                }) { Text(if (busy) "Removing..." else "Remove") } },
                dismissButton = { TextButton(enabled = !busy, onClick = { removal = null }) { Text("Cancel") } }
            )
>>>>>>> 6eab637 (SCRUM-59: Add loading, empty, and error feedback with retry)
        }
    }
}
