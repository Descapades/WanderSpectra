package com.wanderspectra.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.wanderspectra.app.ui.theme.Salsa
import java.text.DateFormat
import java.util.Date

/** Observes only the active child's saved wearable document; never substitutes test coordinates. */
@Composable
fun EmergencyWearableLocation(caregiverId: String, childId: String) {
    val context = LocalContext.current
    var latitude by remember(caregiverId, childId) { mutableStateOf<Double?>(null) }
    var longitude by remember(caregiverId, childId) { mutableStateOf<Double?>(null) }
    var timestamp by remember(caregiverId, childId) { mutableStateOf(0L) }
    var status by remember(caregiverId, childId) { mutableStateOf("Loading last reported wearable location...") }
    var mapError by remember(caregiverId, childId) { mutableStateOf<String?>(null) }
    var cached by remember(caregiverId, childId) { mutableStateOf(false) }

    DisposableEffect(caregiverId, childId) {
        var disposed = false
        val registration = if (caregiverId.isBlank() || childId.isBlank()) {
            status = "No active child is available."
            null
        } else {
            FirebaseFirestore.getInstance().collection("caregivers").document(caregiverId)
                .collection("children").document(childId).collection("wearable").document("device")
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                    if (!disposed) {
                        latitude = null
                        longitude = null
                        if (error != null) {
                            status = "Unable to load wearable location: ${error.localizedMessage ?: "Access failed"}"
                        } else if (snapshot == null || !snapshot.exists()) {
                            status = "No wearable is saved for this child."
                        } else {
                            val lat = (snapshot.get("latitude") as? Number)?.toDouble()
                            val lng = (snapshot.get("longitude") as? Number)?.toDouble()
                            cached = snapshot.metadata.isFromCache
                            timestamp = (snapshot.get("locationTimestamp") as? Number)?.toLong() ?: 0L
                            if (lat != null && lng != null && lat.isFinite() && lng.isFinite()
                                && lat in -90.0..90.0 && lng in -180.0..180.0) {
                                latitude = lat
                                longitude = lng
                                status = "Last reported wearable location"
                            } else {
                                status = "The wearable has not reported a valid location."
                            }
                        }
                    }
                }
        }
        onDispose { disposed = true; registration?.remove() }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(status, fontFamily = Salsa)
        val lat = latitude
        val lng = longitude
        if (lat != null && lng != null) {
            Text("Latitude: $lat\nLongitude: $lng", fontFamily = Salsa)
            Text(
                if (timestamp > 0L) "Reported: ${DateFormat.getDateTimeInstance().format(Date(timestamp))}"
                else "Report time unavailable",
                fontFamily = Salsa
            )
            Text(if (cached) "Saved on this device; server confirmation pending."
                else "Saved location; this may not be the child's current position.", fontFamily = Salsa)
            Button(onClick = {
                mapError = null
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:$lat,$lng?q=$lat,$lng")))
                } catch (_: ActivityNotFoundException) {
                    mapError = "No map app is available. Use the coordinates displayed above."
                } catch (_: SecurityException) {
                    mapError = "Unable to open the map app. Use the coordinates displayed above."
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Open Location in Maps", fontFamily = Salsa)
            }
        }
        mapError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
