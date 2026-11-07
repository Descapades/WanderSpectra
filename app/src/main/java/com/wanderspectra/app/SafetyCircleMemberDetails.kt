package com.wanderspectra.app


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa

@Composable
fun SafetyCircleMemberDetails(
    member: SafetyCircleMember,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardCream
        ),
        border = BorderStroke(1.dp, PrimaryBlue)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = member.name,
                fontFamily = Salsa,
                fontSize = 24.sp,
                color = EmergencyRed
            )

            Text("Relationship: ${member.relationship}")
            Text("Contact: ${member.contactInformation}")

            Text(
                text = if (member.isActive) "Status: Active"
                else "Status: Inactive"
            )

            Text(
                text = "Elopement Alerts: " +
                        if (member.elopementAlertsEnabled) "On" else "Off"
            )

            Text(
                text = "Location Access: " +
                        if (member.locationAccess == "ALWAYS") "Always"
                        else "During Elopement"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onEdit) {
                    Text("Edit")
                }

                OutlinedButton(onClick = onRemove) {
                    Text("Remove")
                }
            }

            TextButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}


