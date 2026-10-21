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
import java.util.concurrent.TimeUnit
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CommunityContent() {

    val repository = remember {
        CommunityAlertRepository()
    }

    var alerts by remember {
        mutableStateOf<List<CommunityAlert>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var selectedAlert by remember {
        mutableStateOf<CommunityAlert?>(null)
    }

    var showingSightingForm by remember {
        mutableStateOf(false)
    }

    var showingEmergencyProfile by remember {
        mutableStateOf(false)
    }

    var emergencyProfile by remember {
        mutableStateOf<ChildProfile?>(null)
    }

    var profileLoading by remember {
        mutableStateOf(false)
    }

    var profileError by remember {
        mutableStateOf<String?>(null)
    }

    var submissionMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        try {
            alerts = repository.getAlerts()
                .filter { it.status == "ACTIVE" }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unable to load alerts"
        } finally {
            isLoading = false
        }
    }

    if (selectedAlert != null) {
        val alert = selectedAlert!!
        if (showingEmergencyProfile) {

            LaunchedEffect(alert.id) {
                profileLoading = true
                profileError = null
                emergencyProfile = null

                FirebaseFirestore.getInstance()
                    .collection("publicEmergencyProfiles")
                    .document(alert.id)
                    .get()
                    .addOnSuccessListener { document ->
                        emergencyProfile =
                            document.toObject(ChildProfile::class.java)

                        profileLoading = false
                    }
                    .addOnFailureListener { exception ->
                        profileError = exception.message
                        profileLoading = false
                    }
            }

            when {
                profileLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                profileError != null -> {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Unable to load Emergency Profile: $profileError",
                            color = PrimaryBlue
                        )

                        TextButton(
                            onClick = {
                                showingEmergencyProfile = false
                            }
                        ) {
                            Text("Back")
                        }
                    }
                }

                else -> {
                    EmergencyProfileContent(
                        profile = emergencyProfile,
                        onBack = {
                            showingEmergencyProfile = false
                        }
                    )
                }
            }

        } else if (showingSightingForm) {
            SightingReportScreen(
                alert = alert,
                onCancel = {
                    showingSightingForm = false
                },
                onSubmitted = {
                    showingSightingForm = false
                    selectedAlert = null
                    submissionMessage = "Sighting submitted successfully."
                }
            )
        } else {
            CommunityAlertDetailScreen(
                alert = alert,
                onBack = {
                    selectedAlert = null
                },
                onReportSighting = {
                    showingSightingForm = true
                },
                onViewChildProfile = {
                    showingEmergencyProfile = true
                }
            )
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        submissionMessage?.let { message ->
            Text(
                text = message,
                color = PrimaryBlue,
                fontFamily = Salsa
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

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
                    text = "Unable to load community alerts:\n$errorMessage",
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
                        CommunityAlertCard(
                            alert = alert,
                            onClick = {
                                selectedAlert = alert
                            }
                        )
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
    val isActive = alert.status == "ACTIVE"

    val alertColor = if (isActive) {
        Color(0xFFC34A5B)
    } else {
        Color(0xFF80D8B5)
    }

    val elapsedTime = alert.createdAt?.let { timestamp ->
        val elapsedMillis = (
                System.currentTimeMillis() - timestamp.toDate().time
                ).coerceAtLeast(0L)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)

        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            else -> "${TimeUnit.MILLISECONDS.toDays(elapsedMillis)} days ago"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4F1E8)
        ),
        border = BorderStroke(1.dp, PrimaryBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "●",
                color = alertColor,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isActive) {
                        "ACTIVE ALERT"
                    } else {
                        "FOUND SAFE"
                    },
                    color = alertColor,
                    fontFamily = Salsa,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Distance will appear when an approved
                // approximate distance is available.
                val distanceMiles = alert.distanceMiles

                if (distanceMiles != null) {
                    Text(
                        text = String.format(
                            java.util.Locale.US,
                            "%.1f Miles Away",
                            distanceMiles
                        ),
                        color = PrimaryBlue,
                        fontFamily = Salsa,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = if (isActive) {
                        "Child last seen near"
                    } else {
                        "Child was found safe"
                    },
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 16.sp
                )

                if (isActive) {
                    Text(
                        text = alert.lastKnownArea.ifBlank {
                            "Location unavailable"
                        },
                        color = PrimaryBlue,
                        fontFamily = Salsa,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (elapsedTime != null) {
                    Text(
                        text = elapsedTime,
                        color = Color(0xFFC34A5B),
                        fontFamily = Salsa,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "VIEW ALERT",
                    color = Color(0xFFC34A5B),
                    fontFamily = Salsa,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
