package com.wanderspectra.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * SCRUM-52
 *
 * Contains only approved information intended for
 * participating Community Alert recipients.
 *
 * Exact live wearable coordinates are not part
 * of this data model.
 */
data class CommunityAlertPublicDetails(
    val alertId: String,
    val childDisplayName: String,
    val approvedDescription: String,
    val alertArea: String,
    val contactInstructions: String
)

/**
 * Reusable Community Alert recipient interface.
 *
 * Eligibility and approved information must be
 * validated by the calling application/backend.
 *
 * Sighting report navigation is delegated
 * through a callback.
 */
@Composable
fun CommunityAlertRecipientScreen(
    alert: CommunityAlertPublicDetails,
    isEligible: Boolean,
    onReportSighting: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "WanderSpectra Community Alert",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (!isEligible) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "This Community Alert is not available to your account.",
                    modifier = Modifier.padding(16.dp)
                )
            }

            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }

        } else {

            Text(
                text = "Missing Child Alert",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "A caregiver has issued a Community Alert requesting assistance locating a missing child.",
                style = MaterialTheme.typography.bodyLarge
            )

            RecipientInformationCard(
                label = "Child",
                value = alert.childDisplayName
            )

            RecipientInformationCard(
                label = "Approved Description",
                value = alert.approvedDescription
            )

            RecipientInformationCard(
                label = "General Alert Area",
                value = alert.alertArea
            )

            RecipientInformationCard(
                label = "Contact Instructions",
                value = alert.contactInstructions
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "Privacy and Safety",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "This alert contains information approved for community sharing."
                    )

                    Text(
                        text = "The child's exact live wearable location is not included."
                    )

                    Text(
                        text = "If you believe you have seen the child, use the private sighting report option below."
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = {
                    onReportSighting(alert.alertId)
                },
                enabled = alert.alertId.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Report a Sighting")
            }

            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close Alert")
            }
        }
    }
}

/**
 * Reusable information card.
 */
@Composable
private fun RecipientInformationCard(
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
