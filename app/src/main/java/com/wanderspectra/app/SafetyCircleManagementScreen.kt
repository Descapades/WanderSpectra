package com.wanderspectra.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanderspectra.app.data.SafetyCircleRepository
import com.wanderspectra.app.model.SafetyCircleMember

/** Repository adapter; the existing SafetyCircleContent remains reusable. */
@Composable
fun SafetyCircleManagementScreen(
    caregiverId: String,
    childId: String,
    repository: SafetyCircleRepository = remember { SafetyCircleRepository() }
) {
    key(caregiverId, childId) {
        var members by remember { mutableStateOf<List<SafetyCircleMember>>(emptyList()) }
        var loading by remember { mutableStateOf(false) }
        var busy by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var editor by remember { mutableStateOf<SafetyCircleMember?>(null) }
        var removal by remember { mutableStateOf<SafetyCircleMember?>(null) }
        val available = caregiverId.isNotBlank() && childId.isNotBlank()
        var disposed by remember { mutableStateOf(false) }
        DisposableEffect(Unit) { onDispose { disposed = true } }
        fun load() {
            if (!available) return
            loading = true
            error = null
            repository.getMembersForChild(caregiverId, childId,
                onSuccess = { if (!disposed) { members = it; loading = false } },
                onError = { if (!disposed) { error = it.localizedMessage ?: "Unable to load members."; loading = false } })
        }
        LaunchedEffect(caregiverId, childId) { load() }
        Column(Modifier.fillMaxSize()) {
            if (!available) {
                Text("Sign in and load a child profile to manage the Safety Circle.", Modifier.padding(16.dp))
            } else {
                if (loading || busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                error?.let { message ->
                    Text(message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { load() }, enabled = !busy && !loading) { Text("Retry loading") }
                }
                Box(Modifier.weight(1f)) {
                    SafetyCircleContent(
                        members = members.map { SafetyCircleMemberUiModel(it.memberId, it.name, it.relationship, it.contactInformation) },
                        onAddMember = {
                            if (!busy && !loading) editor = SafetyCircleMember(caregiverId = caregiverId, childId = childId)
                        },
                        onEditMember = { ui -> if (!busy && !loading) editor = members.find { it.memberId == ui.memberId } },
                        onRemoveMember = { ui -> if (!busy && !loading) removal = members.find { it.memberId == ui.memberId } }
                    )
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
                        } },
                        onError = { if (!disposed) { busy = false; error = it.localizedMessage ?: "Unable to save member." } })
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
                        } },
                        onError = { if (!disposed) { busy = false; error = it.localizedMessage ?: "Unable to remove member." } })
                }) { Text(if (busy) "Removing..." else "Remove") } },
                dismissButton = { TextButton(enabled = !busy, onClick = { removal = null }) { Text("Cancel") } }
            )
        }
    }
}

/** Form only: inputs and callbacks, without navigation or Firebase access. */
@Composable
fun SafetyCircleMemberEditor(
    member: SafetyCircleMember,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (SafetyCircleMember) -> Unit,
    error: String? = null
) {
    var name by remember(member) { mutableStateOf(member.name) }
    var relationship by remember(member) { mutableStateOf(member.relationship) }
    var contact by remember(member) { mutableStateOf(member.contactInformation) }
    val valid = name.isNotBlank() && relationship.isNotBlank() && contact.isNotBlank()
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(if (member.memberId.isBlank()) "Add Safety Circle Member" else "Edit Safety Circle Member") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name (required)") }, enabled = !saving, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(relationship, { relationship = it }, label = { Text("Relationship (required)") }, enabled = !saving, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(contact, { contact = it }, label = { Text("Contact information (required)") }, enabled = !saving, modifier = Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !saving, onClick = {
                onSave(member.copy(name = name.trim(), relationship = relationship.trim(), contactInformation = contact.trim()))
            }) { Text(if (saving) "Saving..." else "Save") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancel") } }
    )
}
