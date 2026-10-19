
package com.wanderspectra.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.firebase.Timestamp
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import com.wanderspectra.app.ui.theme.SecondaryRed
import com.wanderspectra.app.ui.theme.TanSongbird
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.wanderspectra.app.ui.theme.ButtonBlue




private val SightingBlue = Color(0xFF4681B2)
private val SightingDarkBlue = Color(0xFF194F79)
private val SightingRed = Color(0xFFC34A5B)
private val SightingCream = Color(0xFFEEE9D5)
private val SightingBorder = Color(0xFF526B99)
private val SightingButtonPink = Color(0xFFE5A1A7)

@Composable
fun SightingReportScreen(
    alert: CommunityAlert,
    onCancel: () -> Unit,
    onSubmitted: () -> Unit
) {
    val repository = remember { SightingReportRepository() }

    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedTime by remember { mutableStateOf("Just Now") }
    var customHour by remember { mutableStateOf("") }
    var customMinute by remember { mutableStateOf("") }
    var customAmPm by remember { mutableStateOf("AM") }
    var direction by remember { mutableStateOf("") }
    var directionExpanded by remember { mutableStateOf(false) }
    var details by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(alert.id) {
        val latitude = alert.latitude
        val longitude = alert.longitude

        // Use only an approved public alert location.
        if (
            latitude != null &&
            longitude != null &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0
        ) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(latitude, longitude),
                    14f
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Report Sighting",
            fontFamily = TanSongbird,
            fontSize = 20.sp,
            color = SightingRed,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = alert.childName,
            fontFamily = Salsa,
            fontSize = 34.sp,
            color = SecondaryRed,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(22.dp))

        Text(
            text = "Where did you see ${alert.childName}?",
            fontFamily = Salsa,
            fontSize = 24.sp,
            color = SightingDarkBlue
        )

        Spacer(Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, SightingBlue)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { selectedLocation = it }
            ) {
                selectedLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Sighting Location"
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = selectedLocation?.let {
                String.format(
                    Locale.US,
                    "Selected: %.5f, %.5f",
                    it.latitude,
                    it.longitude
                )
            } ?: "Tap the map to select the sighting location.",
            fontFamily = Salsa,
            color = SightingBlue,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(28.dp))

        Text(
            text = "When did you see them?",
            fontFamily = Salsa,
            fontSize = 24.sp,
            color = SightingDarkBlue
        )

        Spacer(Modifier.height(8.dp))

        val timeOptions = listOf(
            "Just Now",
            "Within the last 5 minutes",
            "Within the last 15 minutes",
            "Other Time"
        )

        timeOptions.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedTime = option }
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedTime == option,
                    onClick = { selectedTime = option },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = SightingDarkBlue,
                        unselectedColor = SightingDarkBlue
                    )
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = if (option == "Other Time") {
                        "Other Time:"
                    } else {
                        option
                    },
                    fontFamily = Salsa,
                    fontSize = 16.sp,
                    color = SightingDarkBlue
                )
            }
        }

        if (selectedTime == "Other Time") {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customHour,
                    onValueChange = {
                        if (it.length <= 2 && it.all(Char::isDigit)) {
                            customHour = it
                        }
                    },
                    label = { Text("Hour") },
                    modifier = Modifier.width(82.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                Text(":", fontSize = 22.sp, color = SightingDarkBlue)

                OutlinedTextField(
                    value = customMinute,
                    onValueChange = {
                        if (it.length <= 2 && it.all(Char::isDigit)) {
                            customMinute = it
                        }
                    },
                    label = { Text("Min") },
                    modifier = Modifier.width(82.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                Column {
                    listOf("AM", "PM").forEach { option ->
                        TextButton(
                            onClick = { customAmPm = option },
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Text(
                                text = option,
                                color = if (customAmPm == option) {
                                    SightingDarkBlue
                                } else {
                                    SightingBlue
                                },
                                fontWeight = if (customAmPm == option) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Which direction were they going?",
            fontFamily = Salsa,
            fontSize = 24.sp,
            lineHeight = 29.sp,
            color = SightingDarkBlue
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .align(Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { directionExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 12.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SightingBlue
                )
            ) {
                Text(
                    text = direction.ifBlank { "Direction" },
                    fontFamily = Salsa,
                    fontSize = 18.sp,
                    color = SightingDarkBlue,
                    modifier = Modifier.weight(1f)
                )

                Text("▼", color = SightingDarkBlue)
            }

            DropdownMenu(
                expanded = directionExpanded,
                onDismissRequest = { directionExpanded = false },
                containerColor = Color(0xFF9BBACA)
            ) {
                listOf("North", "South", "East", "West").forEach {
                        option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontFamily = Salsa,
                                fontSize = 18.sp,
                                color = SightingDarkBlue
                            )
                        },
                        onClick = {
                            direction = option
                            directionExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Sighting Details",
            fontFamily = Salsa,
            fontSize = 24.sp,
            color = SightingDarkBlue
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            placeholder = {
                Text(
                    text = "Add any helpful details...",
                    fontFamily = Salsa,
                    color = SightingBlue
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            shape = RoundedCornerShape(10.dp),
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SightingBlue,
                unfocusedBorderColor = SightingBlue
            )
        )

        if (errorMessage.isNotBlank()) {
            Spacer(Modifier.height(12.dp))

            Text(
                text = errorMessage,
                color = SightingRed,
                fontFamily = Salsa
            )
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                val location = selectedLocation

                val sightingDate: Date? = when (selectedTime) {
                    "Just Now" -> Date()

                    "Within the last 5 minutes" ->
                        Date(System.currentTimeMillis() - 5 * 60_000L)

                    "Within the last 15 minutes" ->
                        Date(System.currentTimeMillis() - 15 * 60_000L)

                    else -> {
                        val hour = customHour.toIntOrNull()
                        val minute = customMinute.toIntOrNull()

                        if (
                            hour == null || hour !in 1..12 ||
                            minute == null || minute !in 0..59
                        ) {
                            null
                        } else {
                            val calendar = Calendar.getInstance()

                            calendar.set(
                                Calendar.HOUR_OF_DAY,
                                (hour % 12) +
                                        if (customAmPm == "PM") 12 else 0
                            )
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)

                            if (calendar.timeInMillis >
                                System.currentTimeMillis()
                            ) {
                                calendar.add(Calendar.DAY_OF_YEAR, -1)
                            }

                            calendar.time
                        }
                    }
                }

                when {
                    location == null -> {
                        errorMessage = "Please select a location."
                    }

                    direction.isBlank() -> {
                        errorMessage = "Please select a direction."
                    }

                    alert.id.isBlank() -> {
                        errorMessage = "No Community Alert selected."
                    }

                    sightingDate == null -> {
                        errorMessage = "Enter a valid custom time."
                    }

                    else -> {
                        errorMessage = ""
                        submitting = true

                        val report = SightingReport(
                            communityAlertId = alert.id,
                            childId = alert.childId,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            sightingTime = Timestamp(sightingDate),
                            direction = direction,
                            details = details.trim()
                        )

                        repository.submitReport(report) { success, message ->
                            submitting = false

                            if (success) {
                                onSubmitted()
                            } else {
                                errorMessage = message
                            }
                        }
                    }
                }
            },
            enabled = !submitting,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(54.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue
            ),
            border = BorderStroke(1.dp, SecondaryBlue)
        ) {
            Text(
                text = if (submitting) {
                    "Submitting..."
                } else {
                    "Submit Sighting"
                },
                fontFamily = Salsa,
                fontSize = 20.sp,
                color = SecondaryBlue
            )
        }

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = onCancel,
            enabled = !submitting,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(54.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SightingButtonPink
            ),
            border = BorderStroke(1.dp, SecondaryRed)
        ) {
            Text(
                text = "Cancel",
                fontFamily = Salsa,
                fontSize = 20.sp,
                color = SecondaryRed
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}
