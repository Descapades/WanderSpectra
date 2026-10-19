package com.wanderspectra.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.TanSongbird

@Composable
fun CommunityContent() {

    val repository = remember { CommunityAlertRepository() }

    var alerts by remember {
        mutableStateOf<List<CommunityAlert>>(emptyList())
    }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            alerts = repository.getAlerts()
                .filter { it.status == "ACTIVE" && it.caregiverId.isNotBlank() }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unable to load alerts"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Community",
            color = Color(0xFFC34A5B),
            fontFamily = TanSongbird,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                CircularProgressIndicator(color = PrimaryBlue)
            }

            errorMessage != null -> {
                Text(
                    text = "Unable to load community alerts",
                    color = PrimaryBlue
                )
            }

            alerts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Active Alerts",
                        color = PrimaryBlue,
                        fontFamily = Salsa,
                        fontSize = 24.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alerts) { alert ->
                        CommunityAlertCard(alert = alert)
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityAlertCard(
    alert: CommunityAlert,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEEE9D5)
        ),
        border = BorderStroke(1.dp, PrimaryBlue)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "● ACTIVE ALERT",
                color = Color(0xFFC34A5B),
                fontFamily = Salsa,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Child last seen near",
                color = PrimaryBlue,
                fontFamily = Salsa
            )

            Text(
                text = alert.lastKnownArea.ifBlank {
                    "Location unavailable"
                },
                color = PrimaryBlue,
                fontFamily = Salsa
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "VIEW ALERT",
                color = Color(0xFFC34A5B),
                fontFamily = Salsa,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
