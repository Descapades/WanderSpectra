package com.wanderspectra.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import com.wanderspectra.app.ui.theme.TanSongbird
import java.util.concurrent.TimeUnit
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

private val AlertRed = Color(0xFFC34A5B)
private val AlertCream = Color(0xFFEEE9D5)
private val ButtonPink = Color(0xFFE5A1A7)
private val ProfileBlue = Color(0xFF9BBACA)

private data class AlertSighting(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val sightingTime: Timestamp?,
    val submittedAt: Timestamp?,
    val direction: String,
    val details: String
)

@Composable
fun CommunityAlertDetailScreen(
    alert: CommunityAlert,
    onBack: () -> Unit,
    onReportSighting: () -> Unit,
    onViewChildProfile: (() -> Unit)? = null
) {
    var sightings by remember(alert.id) {
        mutableStateOf<List<AlertSighting>>(emptyList())
    }

    var sightingsError by remember(alert.id) {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(alert.id) {
        val registration = FirebaseFirestore.getInstance()
            .collection("publicSightingReports")
            .whereEqualTo("communityAlertId", alert.id)
            .orderBy("submittedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    android.util.Log.e(
                        "WanderSpectra",
                        "Error loading sighting reports",
                        error
                    )

                    sightingsError = error.message
                        ?: "Unable to load sightings."

                    return@addSnapshotListener
                }

                sightingsError = null

                sightings = snapshot?.documents?.mapNotNull { document ->
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")

                    if (
                        latitude == null ||
                        longitude == null ||
                        !latitude.isFinite() ||
                        !longitude.isFinite() ||
                        latitude !in -90.0..90.0 ||
                        longitude !in -180.0..180.0
                    ) {
                        null
                    } else {
                        AlertSighting(
                            id = document.id,
                            latitude = latitude,
                            longitude = longitude,
                            sightingTime = document.getTimestamp("sightingTime"),
                            submittedAt = document.getTimestamp("submittedAt"),
                            direction = document.getString("direction").orEmpty(),
                            details = document.getString("details").orEmpty()
                        )
                    }
                }.orEmpty()
            }

        onDispose {
            registration.remove()
        }
    }

    val latestSighting = sightings.firstOrNull()

    val latestLocation = latestSighting?.let {
        LatLng(it.latitude, it.longitude)
    }

    val publicLocation = if (
        alert.latitude != null &&
        alert.longitude != null &&
        alert.latitude in -90.0..90.0 &&
        alert.longitude in -180.0..180.0
    ) {
        LatLng(alert.latitude, alert.longitude)
    } else {
        null
    }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(alert.id, publicLocation, latestSighting?.id) {
        val mapLocation = latestLocation ?: publicLocation

        mapLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 13f)
            )
        }
    }

    val lastUpdated = (alert.updatedAt ?: alert.createdAt)?.let {
        val elapsed = (
                System.currentTimeMillis() - it.toDate().time
                ).coerceAtLeast(0L)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed)
        val hours = TimeUnit.MILLISECONDS.toHours(elapsed)

        when {
            minutes < 1 -> "Just Now"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            else -> "${TimeUnit.MILLISECONDS.toDays(elapsed)} days ago"
        }
    } ?: "Unavailable"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Active\nCommunity\nAlert!",
            fontFamily = TanSongbird,
            fontSize = 20.sp,
            lineHeight = 34.sp,
            color = AlertRed,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "${alert.childName.ifBlank { "Child" }} is missing!",
            fontFamily = Salsa,
            fontSize = 34.sp,
            lineHeight = 29.sp,
            color = SecondaryRed,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Last Known Location:",
            fontFamily = Salsa,
            fontSize = 24.sp,
            color = SecondaryBlue,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(8.dp),
            color = AlertCream,
            border = BorderStroke(1.dp, PrimaryBlue)
        ) {
            if (publicLocation != null || sightings.isNotEmpty()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    publicLocation?.let { location ->
                        Marker(
                            state = MarkerState(position = publicLocation),
                            title = "Last Known Area"
                        )
                    }
                    sightings.forEach { sighting ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    sighting.latitude,
                                    sighting.longitude
                                )
                            ),
                            title = if (sighting.id == latestSighting?.id) {
                                "Latest Reported Sighting (Unverified)"
                            } else {
                                "Reported Sighting (Unverified)"
                            },
                            snippet = "Direction: ${sighting.direction}"
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Map location unavailable",
                        fontFamily = Salsa,
                        color = PrimaryBlue,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = alert.lastKnownArea.ifBlank {
                "Location unavailable"
            },
            fontFamily = Salsa,
            fontSize = 18.sp,
            lineHeight = 23.sp,
            color = PrimaryBlue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last Updated: ",
                fontFamily = Salsa,
                fontSize = 17.sp,
                color = AlertRed
            )

            Text(
                text = lastUpdated,
                fontFamily = Salsa,
                fontSize = 17.sp,
                color = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Reported Sightings:",
            fontFamily = Salsa,
            fontSize = 24.sp,
            color = SecondaryBlue,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        when {
            sightingsError != null -> {
                Text(
                    text = sightingsError.orEmpty(),
                    color = SecondaryRed,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            sightings.isEmpty() -> {
                Text(
                    text = "No sightings reported yet.",
                    fontFamily = Salsa,
                    fontSize = 17.sp,
                    color = PrimaryBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }

            else -> {
                val dateFormatter = SimpleDateFormat(
                    "MMM d, h:mm a",
                    Locale.getDefault()
                )

                sightings.forEach { sighting ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = AlertCream,
                        border = BorderStroke(1.dp, PrimaryBlue)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = "Unverified Sighting",
                                fontFamily = Salsa,
                                fontSize = 18.sp,
                                color = SecondaryRed
                            )

                            Text(
                                text = "Seen: " + (
                                        sighting.sightingTime?.toDate()?.let {
                                            dateFormatter.format(it)
                                        } ?: "Unknown"
                                        ),
                                fontFamily = Salsa,
                                color = PrimaryBlue
                            )

                            Text(
                                text = "Direction: ${sighting.direction}",
                                fontFamily = Salsa,
                                color = PrimaryBlue
                            )

                            if (sighting.details.isNotBlank()) {
                                Text(
                                    text = sighting.details,
                                    fontFamily = Salsa,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (onViewChildProfile != null) {
            Button(
                onClick = { onViewChildProfile.invoke() },
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(54.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileBlue
                ),
                border = BorderStroke(1.dp, SecondaryBlue)
            ) {
                Text(
                    text = "View Child Profile",
                    fontFamily = Salsa,
                    fontSize = 20.sp,
                    color = SecondaryBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = onReportSighting,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(54.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonPink
            ),
            border = BorderStroke(1.dp, SecondaryRed)
        ) {
            Text(
                text = "Report Sighting",
                fontFamily = Salsa,
                fontSize = 20.sp,
                color = SecondaryRed
            )
        }
    }
}