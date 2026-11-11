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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import com.wanderspectra.app.ui.theme.TanSongbird

data class SafetyCircleMemberUiModel(
    val memberId: String,
    val name: String,
    val relationship: String,
    val phone: String,
    val isActive: Boolean = true,
    val elopementAlertsEnabled: Boolean = true,
    val locationAccess: String = "DURING_ELOPEMENT"
)

@Composable
fun SafetyCircleContent(
    members: List<SafetyCircleMemberUiModel> = emptyList(),
    onAddMember: () -> Unit = {},
    onViewMember: (SafetyCircleMemberUiModel) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 24.dp,
                top = 24.dp,
                end = 24.dp,
                bottom = 0.dp
            )
    ) {
        Text(
            text = "Safety Circle",
            color = EmergencyRed,
            fontFamily = TanSongbird,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (members.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardCream
                ),
                border = BorderStroke(1.dp, PrimaryBlue),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "No Safety Circle members added yet.",
                        fontFamily = Salsa,
                        fontSize = 22.sp,
                        color = SecondaryBlue
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Use Add Member to begin building the child's trusted response group.",
                        fontFamily = Salsa,
                        fontSize = 16.sp,
                        color = PrimaryBlue
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = members,
                    key = { it.memberId }
                ) { member ->
                    SafetyCircleMemberCard(
                        member = member,
                        onViewMember = onViewMember
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddMember,
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
                text = "Add Member",
                fontFamily = Salsa,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun SafetyCircleMemberCard(
    member: SafetyCircleMemberUiModel,
    onViewMember: (SafetyCircleMemberUiModel) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardCream
        ),
        border = BorderStroke(1.dp, PrimaryBlue),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp
            )
        ) {
            Text(
                text = member.name,
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 24.sp
            )

            Text(
                text = member.relationship,
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 20.dp)
            )

            Text(
                text = if (member.isActive) "● Active" else "● Inactive",
                color = if (member.isActive) Color(0xFF83D9B4) else EmergencyRed,
                fontFamily = Salsa,
                fontSize = 22.sp,
                modifier = Modifier.padding(start = 20.dp)
            )


            Row {
                Text(
                    text = "Elopement Alerts: ",
                    color = EmergencyRed,
                    fontFamily = Salsa,
                    fontSize = 20.sp
                )
                Text(
                    text = if (member.elopementAlertsEnabled) "On" else "Off",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 20.sp
                )
            }

            Row {

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = EmergencyRed)) {
                            append("Location Access: ")
                        }
                        withStyle(SpanStyle(color = PrimaryBlue)) {
                            append(
                                if (member.locationAccess == "ALWAYS") {
                                    "Always"
                                } else {
                                    "During Elopement"
                                }
                            )
                        }
                    },
                    fontFamily = Salsa,
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )

            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onViewMember(member) }
                ) {
                    Text(
                        text = "View Details >",
                        fontFamily = Salsa,
                        fontSize = 16.sp,
                        color = PrimaryBlue
                    )
                }
            }
        }
    }
}
