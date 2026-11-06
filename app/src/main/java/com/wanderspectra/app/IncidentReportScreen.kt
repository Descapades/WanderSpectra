package com.wanderspectra.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.wanderspectra.app.ui.theme.BackgroundCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import android.location.Location
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.TanSongbird
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.ButtonRed
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed

@Composable
fun IncidentReportScreen(
    report: IncidentRecord,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var incidentNotes by remember(report.incidentId) {
        mutableStateOf(report.notes)
    }

    var incidentPossibleTrigger by remember(report.incidentId) {
        mutableStateOf(report.possibleTrigger)
    }

    var showNotesScreen by remember(report.incidentId) {
        mutableStateOf(false)
    }

    var notesDraft by remember(report.incidentId) {
        mutableStateOf(report.notes)
    }

    var destinationAddress by remember(report.incidentId) {
        mutableStateOf("Not recorded")
    }

    LaunchedEffect(
        report.incidentId,
        report.lastKnownLatitude,
        report.lastKnownLongitude,
        report.routePoints
    ) {
        val startLatitude = report.lastKnownLatitude
        val startLongitude = report.lastKnownLongitude

        val farthestPoint = if (
            startLatitude != null &&
            startLongitude != null
        ) {
            report.routePoints.maxByOrNull { point ->
                val pointLatitude = (point["latitude"] as? Number)?.toDouble()
                val pointLongitude = (point["longitude"] as? Number)?.toDouble()

                if (pointLatitude == null || pointLongitude == null) {
                    0f
                } else {
                    val results = FloatArray(1)

                    android.location.Location.distanceBetween(
                        startLatitude,
                        startLongitude,
                        pointLatitude,
                        pointLongitude,
                        results
                    )

                    results[0]
                }
            }
        } else {
            null
        }

        val latitude = (farthestPoint?.get("latitude") as? Number)?.toDouble()
        val longitude = (farthestPoint?.get("longitude") as? Number)?.toDouble()

        destinationAddress = if (
            latitude != null &&
            longitude != null &&
            latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0
        ) {
            withContext(Dispatchers.IO) {
                getDestinationAddress(context, latitude, longitude)
            }
        } else {
            "Not recorded"
        }
    }

    val durationMinutes =
        if (report.startTime != null && report.endTime != null) {
            ((report.endTime.toDate().time -
                    report.startTime.toDate().time) / 60000L)
                .coerceAtLeast(0L)
        } else {
            null
        }

    val statusText = when (report.status.lowercase()) {
        "open", "active" -> "Active"
        "closed", "resolved", "safe" -> "Resolved"
        else -> "Not recorded"
    }

    if (showNotesScreen) {
        IncidentNotesScreen(
            initialNotes = notesDraft,
            initialPossibleTrigger = incidentPossibleTrigger,
            onSave = { updatedNotes, updatedTrigger ->
                IncidentRepository().updateIncidentNotes(
                    incidentId = report.incidentId,
                    notes = updatedNotes,
                    possibleTrigger = updatedTrigger,
                    onSuccess = {
                        incidentNotes = updatedNotes
                        incidentPossibleTrigger = updatedTrigger
                        showNotesScreen = false
                    },
                    onFailure = { exception ->
                        android.widget.Toast.makeText(
                            context,
                            "Failed to save notes: ${exception.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                )
            },
            onCancel = {
                showNotesScreen = false
            }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Incident Report",
            color = EmergencyRed,
            fontFamily = TanSongbird,
            fontSize = 22.sp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardCream)
                .border(
                    1.dp,
                    PrimaryBlue,
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = report.startTime?.let {
                    SimpleDateFormat(
                        "MMMM d, yyyy",
                        Locale.getDefault()
                    ).format(it.toDate())
                } ?: "Date not recorded",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 22.sp
            )

            Text(
                text = when (report.type) {
                    "SAFE_ZONE_EXIT" -> "Safe Zone Exit"
                    else -> "Elopement Incident"
                },
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 14.dp)
            )

            ReportDetail(
                label = "Time",
                value = "${formatReportTime(report.startTime)} – " +
                        formatReportTime(report.endTime)
            )

            ReportDetail(
                label = "Duration",
                value = durationMinutes?.let { "$it minutes" }
                    ?: "Not recorded"
            )

            ReportDetail(
                label = "Safe Zone",
                value = report.safeZoneName
                    ?.takeIf { it.isNotBlank() }
                    ?: "Not recorded"
            )

            ReportDetail(
                label = "Distance Traveled",
                value = report.distanceTraveledMeters?.let {
                    String.format(Locale.US, "%.2f miles", it / 1609.344)
                } ?: "Not recorded"
            )

            ReportDetail(
                label = "Status",
                value = statusText
            )

            if (report.status.lowercase() in listOf("closed", "resolved", "safe")) {
                val resolutionText = when (report.resolutionType) {
                    "RETURNED_TO_SAFE_ZONE" -> "Returned to Safe Zone"
                    "CAREGIVER_CONFIRMED_SAFE" -> "Manually Marked Safe"
                    else -> "Not recorded"
                }

                ReportDetail(
                    label = "Resolution",
                    value = resolutionText
                )
            }

            ReportDetail(
                label = "Farthest Location",
                value = destinationAddress
            )

            Text(
                text = "Route Taken:",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 22.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            IncidentRouteMap(
                routePoints = report.routePoints,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(12.dp)
                    )
            )

            Text(
                text = "Notes:",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 22.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                text = "Possible Trigger:",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 14.dp)
            )

            Text(
                text = incidentPossibleTrigger.ifBlank {
                    "No possible trigger recorded."
                },
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp)
            )

            Text(
                text = "Incident Notes:",
                color = EmergencyRed,
                fontFamily = Salsa,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 16.dp, start = 14.dp)
            )

            Text(
                text = incidentNotes.ifBlank { "No notes recorded" },
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, bottom = 16.dp),
            )

            Button(
                onClick = {
                    notesDraft = incidentNotes
                    showNotesScreen = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(9.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonRed,
                    contentColor = EmergencyRed
                ),
                border = BorderStroke(1.dp, SecondaryRed)
            ) {
                Text(
                    text = if (
                        incidentNotes.isBlank() &&
                        incidentPossibleTrigger.isBlank()
                    ) {
                        "Add Notes"
                    } else {
                        "Edit Notes"
                    },
                    color = SecondaryRed,
                    fontFamily = Salsa,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = onBack,
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
                    text = "Back",
                    color = SecondaryBlue,
                    fontFamily = Salsa,
                    fontSize = 20.sp
                )
            }
        }
        if (showNotesScreen) {
            IncidentNotesScreen(
                initialNotes = notesDraft,
                initialPossibleTrigger = incidentPossibleTrigger,
                onSave = { updatedNotes, updatedTrigger ->
                    IncidentRepository().updateIncidentNotes(
                        incidentId = report.incidentId,
                        notes = updatedNotes,
                        possibleTrigger = updatedTrigger,
                        onSuccess = {
                            incidentNotes = updatedNotes
                            incidentPossibleTrigger = updatedTrigger
                            showNotesScreen = false
                        },
                        onFailure = { exception ->
                            android.widget.Toast.makeText(
                                context,
                                "Failed to save notes: ${exception.message}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },
                onCancel = {
                    showNotesScreen = false
                }
            )
        }
    }
}

@Composable
private fun ReportDetail(
    label: String,
    value: String
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = EmergencyRed)) {
                append("$label: ")
            }
            withStyle(SpanStyle(color = PrimaryBlue)) {
                append(value)
            }
        },
        fontFamily = Salsa,
        fontSize = 20.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp)
    )
}

private fun formatReportTime(timestamp: Timestamp?): String {
    return timestamp?.let {
        SimpleDateFormat(
            "h:mm a",
            Locale.getDefault()
        ).format(it.toDate())
    } ?: "Not recorded"
}

@Composable
private fun IncidentRouteMap(
    routePoints: List<Map<String, Any>>,
    modifier: Modifier = Modifier
) {
    val coordinates = remember(routePoints) {
        routePoints.mapNotNull { point ->
            val latitude = (point["latitude"] as? Number)?.toDouble()
            val longitude = (point["longitude"] as? Number)?.toDouble()

            if (
                latitude != null &&
                longitude != null &&
                latitude in -90.0..90.0 &&
                longitude in -180.0..180.0
            ) {
                LatLng(latitude, longitude)
            } else {
                null
            }
        }
    }

    if (coordinates.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No GPS route recorded.",
                color = PrimaryBlue,
                fontFamily = Salsa
            )
        }
        return
    }

    val startPoint = coordinates.first()

    val farthestPoint = coordinates.maxByOrNull { point ->
        val results = FloatArray(1)

        Location.distanceBetween(
            startPoint.latitude,
            startPoint.longitude,
            point.latitude,
            point.longitude,
            results
        )

        results[0]
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            coordinates.first(),
            16f
        )
    }

    LaunchedEffect(coordinates) {
        if (coordinates.size > 1) {
            val bounds = LatLngBounds.builder().apply {
                coordinates.forEach { include(it) }
            }.build()

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 60)
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        Polyline(
            points = coordinates,
            color = Color.Blue,
            width = 8f
        )

        Marker(
            state = MarkerState(position = coordinates.first()),
            title = "Safe Zone Exit"
        )

        Marker(
            state = MarkerState(position = coordinates.last()),
            title = "Returned to Safe Zone",
            icon = BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_GREEN
            )
        )

        farthestPoint?.let { point ->
            Marker(
                state = MarkerState(position = point),
                title = "Farthest Recorded Location",
                icon = rememberFarthestLocationStar()
            )
        }
    }
}

@Composable
private fun rememberFarthestLocationStar(): BitmapDescriptor {
    val context = LocalContext.current

    return remember(context) {
        val size = 96
        val bitmap = Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(204, 67, 87)
            style = Paint.Style.FILL
        }

        val path = Path()
        val center = size / 2f
        val outerRadius = 42f
        val innerRadius = 19f

        for (i in 0 until 10) {
            val angle = Math.PI * i / 5.0 - Math.PI / 2.0
            val radius = if (i % 2 == 0) outerRadius else innerRadius

            val x = center + radius * kotlin.math.cos(angle).toFloat()
            val y = center + radius * kotlin.math.sin(angle).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        path.close()
        canvas.drawPath(path, paint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

