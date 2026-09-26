package com.wanderspectra.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa

data class SafetyCircleMemberUiModel(
    val memberId: String,
    val name: String,
    val relationship: String,
    val contactInformation: String
)

@Composable
fun SafetyCircleContent(
    members: List<SafetyCircleMemberUiModel> = emptyList(),
    onAddMember: () -> Unit = {},
    onEditMember: (SafetyCircleMemberUiModel) -> Unit = {},
    onRemoveMember: (SafetyCircleMemberUiModel) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Safety Circle",
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Manage the trusted people included in this child's Safety Circle.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddMember,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Safety Circle Member")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (members.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "No Safety Circle members added yet.",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Use Add Safety Circle Member to begin building the child's trusted response group.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = members,
                    key = { it.memberId }
                ) { member ->
                    SafetyCircleMemberCard(
                        member = member,
                        onEditMember = onEditMember,
                        onRemoveMember = onRemoveMember
                    )
                }
            }
        }
    }
}

@Composable
private fun SafetyCircleMemberCard(
    member: SafetyCircleMemberUiModel,
    onEditMember: (SafetyCircleMemberUiModel) -> Unit,
    onRemoveMember: (SafetyCircleMemberUiModel) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = member.name,
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = member.relationship,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = member.contactInformation,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onEditMember(member) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }

                OutlinedButton(
                    onClick = { onRemoveMember(member) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Remove")
                }
            }
        }
    }
}
