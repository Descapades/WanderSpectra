package com.wanderspectra.app

import SightingReportDetailScreen
import android.location.Geocoder
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import com.wanderspectra.app.ui.theme.TanSongbird
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.google.android.gms.maps.model.LatLngBounds

private val AlertRed = Color(0xFFC34A5B)
private val AlertCream = Color(0xFFEEE9D5)
private val ButtonPink = Color(0xFFE5A1A7)
private val ProfileBlue = Color(0xFF9BBACA)

data class AlertSighting(
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
    var selectedSighting by remember(alert.id) { mutableStateOf<AlertSighting?>(null) }

    if (selectedSighting != null) {
        SightingReportDetailScreen(
            sighting = selectedSighting!!,
            onBack = { selectedSighting = null }
        )
        return
    }

    var sightings by remember(alert.id) { mutableStateOf<List<AlertSighting>>(emptyList()) }
    var sightingsError by remember(alert.id) { mutableStateOf<String?>(null) }

    DisposableEffect(alert.id) {
        val registration = FirebaseFirestore.getInstance()
            .collection("sightingReports")
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

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(alert.id, publicLocation, sightings) {
        val locations = buildList {
            publicLocation?.let { add(it) }

            sightings.forEach { sighting ->
                add(
                    LatLng(
                        sighting.latitude,
                        sighting.longitude
                    )
                )
            }
        }

        when (locations.size) {
            0 -> Unit

            1 -> {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        locations.first(),
                        14f
                    )
                )
            }

            else -> {
                val bounds = LatLngBounds.builder().apply {
                    locations.forEach { include(it) }
                }.build()

                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        100
                    )
                )
            }
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
                            state = MarkerState(position = location),
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
                            title = "Reported Sighting (Unverified)",
                            snippet = "Direction: ${sighting.direction}",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_AZURE
                            )
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
            text = publicLocationAddress,
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
                    "MMM d, yyyy 'at' h:mm a",
                    Locale.getDefault()
                )

                sightings.forEach { sighting ->
                    Surface(
                        onClick = {
                            selectedSighting = sighting
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = CardCream,
                        border = BorderStroke(1.dp, PrimaryBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sighting.sightingTime?.toDate()?.let {
                                    dateFormatter.format(it)
                                } ?: "Unknown Date",
                                fontFamily = Salsa,
                                fontSize = 18.sp,
                                color = EmergencyRed,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "›",
                                fontFamily = Salsa,
                                fontSize = 28.sp,
                                color = EmergencyRed
                            )
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