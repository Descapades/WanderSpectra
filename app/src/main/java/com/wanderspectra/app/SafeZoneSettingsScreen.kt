package com.wanderspectra.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.wanderspectra.app.ui.theme.TanSongbird
import android.location.Geocoder
import android.os.Build
import java.util.Locale
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.input.KeyboardType
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.SecondaryBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SafeZoneSettingsContent(
    childId: String,
    editingZoneId: String? = null,
    onSaveSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("Home") }
    var radiusText by remember { mutableStateOf("500") }
    var latText by remember { mutableStateOf("30.43") }
    var lngText by remember { mutableStateOf("-86.57") }
    var addressText by remember { mutableStateOf("") }
    var monitoring by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("Not saved yet") }
    var isSaving by remember { mutableStateOf(false) }
    var editingZone by remember { mutableStateOf<SafeZone?>(null) }
    var childProfiles by remember { mutableStateOf<List<ChildProfile>>(emptyList()) }
    var childMonitoring by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    LaunchedEffect(Unit) {
        ChildProfileRepository().getChildProfiles(
            onSuccess = { children ->
                childProfiles = children
            },
            onFailure = { exception ->
                Log.e(
                    "SafeZoneSettings",
                    "Failed to load child profiles",
                    exception
                )
                status = "Unable to load child profiles."
            }
        )
    }

    LaunchedEffect(editingZoneId) {
        if (editingZoneId != null) {
            SafeZoneRepository().getById(editingZoneId) { zone ->
                if (zone != null) {
                    editingZone = zone
                    name = zone.name
                    radiusText = zone.radius.toString()
                    latText = zone.lat.toString()
                    lngText = zone.lng.toString()
                    monitoring = zone.monitoringEnabled
                    childMonitoring = zone.childMonitoring
                    Log.d(
                        "SafeZoneDebug",
                        "Loaded ${zone.name} assignments: ${zone.childMonitoring}"
                    )

                    addressText = "Loading address..."

                    scope.launch {
                        val address = withContext(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())

                                @Suppress("DEPRECATION")
                                geocoder.getFromLocation(zone.lat, zone.lng, 1)
                                    ?.firstOrNull()
                                    ?.getAddressLine(0)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        addressText = address ?: ""
                    }
                    status = "Safe Zone loaded."
                } else {
                    status = "Unable to load Safe Zone."
                }
            }
        }
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Safe Zone Settings",
            color = EmergencyRed,
            fontFamily = TanSongbird,
            fontSize = 16.sp
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Children to Monitor",
                color = SecondaryBlue,
                fontFamily = Salsa,
                fontSize = 14.sp
            )

            childProfiles.forEach { child ->
                val childName = child.preferredName.ifBlank {
                    child.fullName
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = childName,
                        color = SecondaryBlue,
                        fontFamily = Salsa,
                        fontSize = 14.sp
                    )

                    Switch(
                        checked = childMonitoring[child.childId] ?: false,
                        onCheckedChange = { enabled ->
                            childMonitoring = childMonitoring + (
                                    child.childId to enabled
                                    )
                        }
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Zone Name",
                color = SecondaryBlue,
                fontFamily = Salsa,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardCream,
                    unfocusedContainerColor = CardCream,
                    focusedBorderColor = SecondaryBlue,
                    unfocusedBorderColor = SecondaryBlue,
                    cursorColor = SecondaryBlue,
                    focusedTextColor = Color.DarkGray,
                    unfocusedTextColor = Color.DarkGray
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Radius (ft)",
                color = SecondaryBlue,
                fontFamily = Salsa,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = radiusText,
                onValueChange = { radiusText = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardCream,
                    unfocusedContainerColor = CardCream,
                    focusedBorderColor = SecondaryBlue,
                    unfocusedBorderColor = SecondaryBlue,
                    cursorColor = SecondaryBlue,
                    focusedTextColor = Color.DarkGray,
                    unfocusedTextColor = Color.DarkGray
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Street Address",
                color = SecondaryBlue,
                fontFamily = Salsa,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = addressText,
                onValueChange = { addressText = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                placeholder = {
                    Text("Enter street, city, state, ZIP")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0EEE8),
                    unfocusedContainerColor = Color(0xFFF0EEE8),
                    focusedBorderColor = SecondaryBlue,
                    unfocusedBorderColor = SecondaryBlue,
                    cursorColor = SecondaryBlue,
                    focusedTextColor = Color.DarkGray,
                    unfocusedTextColor = Color.DarkGray
                )
            )
        }

        Button(
            onClick = {
                if (addressText.isBlank()) {
                    status = "Please enter an address"
                } else {
                    status = "Finding address..."

                    findSafeZoneAddress(
                        context = context,
                        address = addressText.trim()
                    ) { latitude, longitude ->
                        if (latitude != null && longitude != null) {
                            latText = latitude.toString()
                            lngText = longitude.toString()
                            status = "Address found. Please verify the map location."
                        } else {
                            status = "Address not found. Please check the address."
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SecondaryBlue),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue,
                contentColor = SecondaryBlue
            )
        ) {
            Text(
                text = "Find Address",
                fontFamily = Salsa,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
        val mapLatitude = latText.toDoubleOrNull()
        val mapLongitude = lngText.toDoubleOrNull()
        val mapRadius = radiusText.toIntOrNull()

        if (
            mapLatitude != null &&
            mapLongitude != null &&
            mapLatitude.isFinite() &&
            mapLongitude.isFinite() &&
            mapLatitude in -90.0..90.0 &&
            mapLongitude in -180.0..180.0 &&
            mapRadius != null &&
            mapRadius > 0
        ) {
            SafeZoneMap(
                latitude = mapLatitude,
                longitude = mapLongitude,
                radiusFeet = mapRadius
            )
        }
        Text(
            text = status,
            color = PrimaryBlue,
            fontFamily = Salsa,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            enabled = !isSaving,
            onClick = {
                if (isSaving) return@Button

                if (name.isBlank()) {
                    status = "Name cannot be empty"
                    return@Button
                }
                val lat = latText.toDoubleOrNull()
                val lng = lngText.toDoubleOrNull()
                if (lat == null || lng == null) {
                    status = "Lat and lng must be numbers"
                    return@Button
                }
                if (editingZoneId != null && editingZone?.id != editingZoneId) {
                    status = "Safe Zone has not loaded. Please try again."
                    return@Button
                }

                isSaving = true


                val zone = editingZone?.copy(
                    childId = childId,
                    name = name,
                    radius = radiusText.toIntOrNull() ?: 500,
                    monitoringEnabled = monitoring,
                    childMonitoring = childMonitoring,
                    lat = lat,
                    lng = lng
                ) ?: SafeZone(
                    childId = childId,
                    name = name,
                    radius = radiusText.toIntOrNull() ?: 500,
                    monitoringEnabled = monitoring,
                    childMonitoring = childMonitoring,
                    lat = lat,
                    lng = lng
                )

                val repository = SafeZoneRepository()

                val onSaved: (Boolean, String, SafeZone?) -> Unit =
                    { success, message, savedZone ->
                        isSaving = false
                        status = message

                        if (success) {
                            val zoneToSend = savedZone ?: zone

                            onSaveSuccess()

                            WearConnectionManager.getAvailableWearables(context) { nodes ->
                                if (nodes.isEmpty()) {
                                    Log.w("SafeZoneSync", "No connected wearable available")
                                    status = "Safe Zone saved. Watch not connected."
                                } else {
                                    val node = nodes.first()

                                    WearConnectionManager.sendSafeZoneToWearable(
                                        context = context,
                                        nodeId = node.id,
                                        safeZone = zoneToSend,
                                        childId = childId
                                    ) { sent ->
                                        status = if (sent) {
                                            "Safe Zone saved. Sent to watch."
                                        } else {
                                            "Safe Zone saved. Watch sync failed."
                                        }
                                    }
                                }
                            }
                        }
                    }
                if (editingZoneId == null) {
                    repository.create(zone, onSaved)
                } else {
                    repository.update(zone, onSaved)
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SecondaryBlue),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue,
                contentColor = SecondaryBlue
            )
        ) {
            Text(
                text = "Save Zone",
                fontFamily = Salsa,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}


private fun findSafeZoneAddress(
    context: Context,
    address: String,
    onResult: (Double?, Double?) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        geocoder.getFromLocationName(
            address,
            1,
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<android.location.Address>) {
                    val result = addresses.firstOrNull()
                    onResult(result?.latitude, result?.longitude)
                }

                override fun onError(errorMessage: String?) {
                    onResult(null, null)
                }
            }
        )
    } else {
        Thread {
            val result = try {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(address, 1)?.firstOrNull()
            } catch (e: Exception) {
                null
            }

            Handler(Looper.getMainLooper()).post {
                onResult(result?.latitude, result?.longitude)
            }
        }.start()
    }
}


@Composable
fun SafeZoneMap(
    latitude: Double,
    longitude: Double,
    radiusFeet: Int
) {
    val center = remember(latitude, longitude) {
        LatLng(latitude, longitude)
    }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(center, radiusFeet) {
        cameraPositionState.move(
            CameraUpdateFactory.newLatLngZoom(
                center,
                16f
            )
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            Color(0xFF4681B2)
        )
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Circle(
                center = center,
                radius = radiusFeet * 0.3048,
                strokeColor = Color(0xFF4681B2),
                strokeWidth = 3f,
                fillColor = Color(0x334681B2)
            )
        }
    }
}