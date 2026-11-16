package com.wanderspectra.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.text.style.TextAlign
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.TanSongbird
import androidx.compose.ui.graphics.Color


@Composable
fun PrivacyPermissionsContent() {
    var childLocationEnabled by remember { mutableStateOf(false) }
    var caregiverLocationEnabled by remember { mutableStateOf(false) }
    var safetyCircleEnabled by remember { mutableStateOf(false) }
    var communityAlertsEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 0.dp
            )
            .background(
                color = CardCream,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = PrimaryBlue,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Privacy &\nPermissions",
            color = EmergencyRed,
            fontFamily = TanSongbird,
            fontSize = 20.sp,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardCream
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 4.dp,
                        end = 4.dp,
                        bottom = 4.dp
                    )
            ) {
                Text(
                    text = "Location Permissions",
                    color = PrimaryBlue,
                    fontFamily = TanSongbird,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                PrivacySettingRow(
                    title = "Child Location",
                    description = "Allow WanderSpectra to access the wearable's location for Safe Zone monitoring and location features.",
                    checked = childLocationEnabled,
                    onCheckedChange = { childLocationEnabled = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacySettingRow(
                    title = "Caregiver Location",
                    description = "Allow caregiver location when needed for map and location features.",
                    checked = caregiverLocationEnabled,
                    onCheckedChange = { caregiverLocationEnabled = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Community Privacy",
                    color = PrimaryBlue,
                    fontFamily = TanSongbird,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                PrivacySettingRow(
                    title = "Share with Safety Circle",
                    description = "Allow trusted Safety Circle members to receive information during an active elopement.",
                    checked = safetyCircleEnabled,
                    onCheckedChange = { safetyCircleEnabled = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacySettingRow(
                    title = "Community Alerts",
                    description = "Allow the caregiver to issue Community Alerts during an active elopement.",
                    checked = communityAlertsEnabled,
                    onCheckedChange = { communityAlertsEnabled = it }
                )
            }
        }
    }
}


@Composable
private fun PrivacySettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            color = SecondaryBlue,
            fontFamily = Salsa,
            fontSize = 22.sp,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryBlue,
                    checkedBorderColor = PrimaryBlue,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = EmergencyRed.copy(alpha = 0.35f),
                    uncheckedBorderColor = EmergencyRed
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = description,
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
