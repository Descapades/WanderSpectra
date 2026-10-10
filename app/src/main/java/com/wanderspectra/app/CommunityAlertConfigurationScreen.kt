package com.wanderspectra.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * SCRUM-50
 *
 * Public-facing information selected for a Community Alert.
 *
 * Exact wearable coordinates are intentionally not included
 * in this model.
 */
data class CommunityAlertDraft(
    val childDisplayName: String,
    val publicDescription: String,
    val alertArea: String,
    val contactInstructions: String
)

/**
 * Caregiver review screen for an optional Community Alert.
 *
 * This component does not send notifications directly.
 * The parent application supplies the approved sending action.
 */
@Composable
fun CommunityAlertConfigurationScreen(
    draft: CommunityAlertDraft,
    isElopementConfirmed: Boolean,
    onSendConfirmed: (CommunityAlertDraft) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmation by remember {
        mutableStateOf(false)
    }

    val canSend =
        isElopementConfirmed &&
        draft.childDisplayName.isNotBlank() &&
        draft.alertArea.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Community Alert",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Review the information before notifying nearby WanderSpectra participants.",
            style = MaterialTheme.typography.bodyLarge
        )

        if (!isElopementConfirmed) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Community Alerts are available only during a confirmed elopement.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            text = "Information to Be Shared",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        CommunityAlertInformationCard(
            label = "Child",
            value = draft.childDisplayName
        )

        CommunityAlertInformationCard(
            label = "Description",
            value = draft.publicDescription
        )

        CommunityAlertInformationCard(
            label = "Community Alert Area",
            value = draft.alertArea
        )

        CommunityAlertInformationCard(
            label = "Contact Instructions",
            value = draft.contactInstructions
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Privacy Notice",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "This Community Alert preview does not include the child's exact live wearable location."
                )

                Text(
                    text = "Community participants receive only the information approved for the alert."
                )
            }
        }

        if (!canSend && isElopementConfirmed) {
            Text(
                text = "A child name and Community Alert area are required before continuing.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                showConfirmation = true
            },
            enabled = canSend,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Review and Confirm Alert")
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }

    if (showConfirmation && canSend) {
        AlertDialog(
            onDismissRequest = {
                showConfirmation = false
            },
            title = {
                Text("Send Community Alert?")
            },
            text = {
                Text(
                    "You are about to authorize a Community Alert " +
                    "for ${draft.childDisplayName} within ${draft.alertArea}. " +
                    "Do you want to continue?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = false
                        onSendConfirmed(draft)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmation = false
                    }
                ) {
                    Text("Go Back")
                }
            }
        )
    }
}

/**
 * Reusable display component for alert review information.
 */
@Composable
private fun CommunityAlertInformationCard(
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = value.ifBlank { "Not provided" },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
