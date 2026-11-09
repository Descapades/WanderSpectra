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
import android.location.Geocoder
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun CommunityContent(
    onBackHandlerChanged: (() -> Unit) -> Unit,
    onBackAvailabilityChanged: (Boolean) -> Unit
) {
    val repository = remember { CommunityAlertRepository() }
    var alerts by remember { mutableStateOf<List<CommunityAlert>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedAlert by remember { mutableStateOf<CommunityAlert?>(null) }
    var showingSightingForm by remember { mutableStateOf(false) }
    var showingEmergencyProfile by remember { mutableStateOf(false) }
    var sightingBackHandler by remember { mutableStateOf<(() -> Unit)?>(null) }
    var emergencyProfile by remember { mutableStateOf<ChildProfile?>(null) }
    var profileLoading by remember { mutableStateOf(false) }
    var profileError by remember { mutableStateOf<String?>(null) }
    var submissionMessage by remember { mutableStateOf<String?>(null) }

    SideEffect {
        onBackAvailabilityChanged(selectedAlert != null)

        onBackHandlerChanged {
            when {
                showingEmergencyProfile -> showingEmergencyProfile = false
                showingSightingForm -> showingSightingForm = false
                sightingBackHandler != null -> sightingBackHandler?.invoke()
                selectedAlert != null -> selectedAlert = null
            }
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            try {
                alerts = repository.getAlerts().sortedWith(
                    compareByDescending<CommunityAlert> { it.status == "ACTIVE" }
                        .thenByDescending { it.createdAt?.seconds ?: 0L }
                )
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unable to load alerts"
            } finally {
                isLoading = false
            }

            delay(30_000L)
        }
    }

    if (selectedAlert != null) {
        val alert = selectedAlert!!
        if (showingEmergencyProfile) {

            LaunchedEffect(alert.id, showingEmergencyProfile) {
                profileLoading = true
                profileError = null
                emergencyProfile = null

                try {
                    val user = FirebaseAuth.getInstance().currentUser
                        ?: throw Exception("User is not authenticated")

                    val idToken = user.getIdToken(false).await().token
                        ?: throw Exception("Unable to retrieve Firebase token")

                    val connection = (
                            URL("https://wanderspectra-alerts.destin-champlin.workers.dev/get-emergency-profile")
                                .openConnection() as HttpURLConnection
                            )

                    try {
                        connection.requestMethod = "POST"
                        connection.setRequestProperty("Authorization", "Bearer $idToken")
                        connection.setRequestProperty("Content-Type", "application/json")
                        connection.connectTimeout = 15000
                        connection.readTimeout = 15000
                        connection.doOutput = true

                        val requestBody = JSONObject()
                            .put("alertId", alert.id)
                            .toString()

                        withContext(Dispatchers.IO) {
                            connection.outputStream.use {
                                it.write(requestBody.toByteArray(Charsets.UTF_8))
                            }

                            val responseCode = connection.responseCode

                            if (responseCode != HttpURLConnection.HTTP_OK) {
                                throw Exception("Emergency Profile request failed: $responseCode")
                            }

                            val response = connection.inputStream.bufferedReader().use {
                                it.readText()
                            }

                            val profileJson = JSONObject(response).getJSONObject("profile")

                            emergencyProfile = ChildProfile(
                                fullName = profileJson.optString("fullName"),
                                preferredName = profileJson.optString("preferredName"),
                                birthdate = profileJson.optString("birthdate"),
                                photoUrl = profileJson.optString("photoUrl"),
                                height = profileJson.optString("height"),
                                weight = profileJson.optString("weight"),
                                hairColor = profileJson.optString("hairColor"),
                                eyeColor = profileJson.optString("eyeColor"),
                                communicationStyle = profileJson.optString("communicationStyle"),
                                helpfulCommunicationInfo = profileJson.optString("helpfulCommunicationInfo"),
                                sensorySensitivities = profileJson.optString("sensorySensitivities"),
                                elopementTriggers = profileJson.optString("elopementTriggers"),
                                calmingStrategies = profileJson.optString("calmingStrategies"),
                                safetyConcerns = profileJson.optString("safetyConcerns"),
                                allergies = profileJson.optString("allergies"),
                                medicalConsiderations = profileJson.optString("medicalConsiderations"),
                                criticalInformation = profileJson.optString("criticalInformation")
                            )
                        }
                    } finally {
                        connection.disconnect()
                    }
                } catch (e: Exception) {
                    profileError = e.message ?: "Unable to load Emergency Profile"
                } finally {
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
                },
                onSightingBackHandlerChanged = { handler ->
                    sightingBackHandler = handler
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
    val context = LocalContext.current

    val publicLocation = if (
        alert.approximateLatitude != null &&
        alert.approximateLongitude != null &&
        alert.approximateLatitude in -90.0..90.0 &&
        alert.approximateLongitude in -180.0..180.0
    ) {
        LatLng(alert.approximateLatitude, alert.approximateLongitude)
    } else {
        null
    }

    var publicLocationAddress by remember(publicLocation) {
        mutableStateOf("Loading location...")
    }

    LaunchedEffect(publicLocation) {
        val location = publicLocation

        if (location == null) {
            publicLocationAddress = "Location unavailable"
            return@LaunchedEffect
        }

        publicLocationAddress = "Loading location..."

        publicLocationAddress = withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                Geocoder(context, Locale.getDefault())
                    .getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    )
                    ?.firstOrNull()
                    ?.getAddressLine(0)
                    ?: "Approximate location unavailable"
            } catch (e: Exception) {
                "Approximate location unavailable"
            }
        }
    }

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
                        text = publicLocationAddress,
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
