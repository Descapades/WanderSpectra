package com.wanderspectra.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/** Holds only a navigation request. Firestore ownership is checked before rendering. */
object SightingReportNavigation {
    var request by mutableStateOf<Pair<String, String>?>(null)
        private set
    fun receive(intent: Intent?) {
        val alert = intent?.getStringExtra("sightingAlertId").orEmpty()
        val report = intent?.getStringExtra("sightingReportId").orEmpty()
        if (alert.isNotBlank() && report.isNotBlank()) request = alert to report
    }
    fun clear() { request = null }
}

@Composable
fun CaregiverSightingNotifications(caregiverId: String, childId: String) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    var latest by remember(caregiverId, childId) { mutableStateOf<Pair<String, String>?>(null) }
    var listenerError by remember(caregiverId, childId) { mutableStateOf<String?>(null) }
    DisposableEffect(caregiverId, childId) {
        val listeners = mutableMapOf<String, ListenerRegistration>()
        var disposed = false
        val seen = context.getSharedPreferences("sighting_seen_$caregiverId", Context.MODE_PRIVATE)
        val alertsListener = if (caregiverId.isNotBlank() && childId.isNotBlank()) {
            db.collection("communityAlerts").whereEqualTo("caregiverId", caregiverId)
                .whereEqualTo("childId", childId)
                .addSnapshotListener { snapshot, error ->
                    if (!disposed) {
                        if (error != null) {
                            listenerError = "Sighting notifications: ${error.localizedMessage}"
                            listeners.values.forEach { it.remove() }
                            listeners.clear()
                        } else if (snapshot != null) {
                            val active = snapshot.documents.filter { it.getString("status") == "ACTIVE" }.map { it.id }.toSet()
                            (listeners.keys.toSet() - active).forEach { listeners.remove(it)?.remove() }
                            active.forEach { alertId ->
                                if (!listeners.containsKey(alertId)) {
                                    listeners[alertId] = db.collection("sightingReports")
                                        .whereEqualTo("communityAlertId", alertId)
                                        .whereEqualTo("childId", childId)
                                        .addSnapshotListener reports@{ reports, reportError ->
                                            if (disposed) return@reports
                                            if (reportError != null) {
                                                listenerError = "Sighting reports: ${reportError.localizedMessage}"
                                                return@reports
                                            }
                                            reports?.documents?.forEach { report ->
                                                if (report.metadata.hasPendingWrites() || report.getTimestamp("submittedAt") == null) return@forEach
                                                if (report.getString("communityAlertId") != alertId || report.getString("childId") != childId) return@forEach
                                                val reportId = report.id
                                                val seenKey = "$alertId/$reportId"
                                                if (!seen.getBoolean(seenKey, false)) {
                                                    latest = alertId to reportId
                                                    showSightingNotification(context, alertId, reportId)
                                                    seen.edit().putBoolean(seenKey, true).apply()
                                                }
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
        } else null
        onDispose {
            disposed = true
            alertsListener?.remove()
            listeners.values.forEach { it.remove() }
        }
    }
    Column(Modifier.fillMaxWidth()) {
        listenerError?.let { Text(it, Modifier.padding(8.dp), color = MaterialTheme.colorScheme.error) }
        latest?.let { target ->
            TextButton(onClick = {
                SightingReportNavigation.receive(Intent().putExtra("sightingAlertId", target.first).putExtra("sightingReportId", target.second))
            }) { Text("View new sighting report") }
        }
    }
    val target = SightingReportNavigation.request
    if (target != null) {
        CaregiverSightingReportDialog(caregiverId, target.first, target.second, onClose = { SightingReportNavigation.clear() })
    }
}

private fun showSightingNotification(context: Context, alertId: String, reportId: String) {
    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "caregiver_sightings"
    if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(channelId, "Sighting reports", NotificationManager.IMPORTANCE_HIGH))
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        data = android.net.Uri.Builder().scheme("wanderspectra").authority("sighting").appendPath(alertId).appendPath(reportId).build()
        putExtra("sightingAlertId", alertId)
        putExtra("sightingReportId", reportId)
    }
    val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    manager.notify("$alertId/$reportId", 54, NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("New sighting report")
        .setContentText("A sighting was reported for your child's active alert. Tap to view.")
        .setContentIntent(pending).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).build())
}

@Composable
private fun CaregiverSightingReportDialog(caregiverId: String, alertId: String, reportId: String, onClose: () -> Unit) {
    var text by remember(caregiverId, alertId, reportId) { mutableStateOf("Loading report...") }
    LaunchedEffect(caregiverId, alertId, reportId) {
        text = "Loading report..."
        if (caregiverId.isBlank()) {
            text = "Sign in to view this report."
        } else {
            val db = FirebaseFirestore.getInstance()
            db.collection("communityAlerts").document(alertId).get()
                .addOnSuccessListener { alert ->
                    if (!alert.exists() || alert.getString("caregiverId") != caregiverId || alert.getString("status") != "ACTIVE") {
                        text = "This active alert is unavailable for this caregiver."
                    } else {
                        db.collection("sightingReports").document(reportId).get()
                            .addOnSuccessListener { report ->
                                if (!report.exists() || report.getString("communityAlertId") != alertId || report.getString("childId") != alert.getString("childId")) {
                                    text = "The report does not belong to this alert."
                                } else {
                                    text = listOf(
                                        "Child: ${alert.getString("childName").orEmpty()}",
                                        "Alert: $alertId",
                                        "Report: $reportId",
                                        "Sighting time: ${report.getTimestamp("sightingTime")?.toDate() ?: "Not supplied"}",
                                        "Location: ${report.getString("locationDescription").orEmpty()}",
                                        "Coordinates: ${report.getDouble("latitude")}, ${report.getDouble("longitude")}",
                                        "Direction: ${report.getString("direction").orEmpty()}",
                                        "Details: ${report.getString("details").orEmpty()}"
                                    ).joinToString("\n\n")
                                }
                            }.addOnFailureListener { text = it.localizedMessage ?: "Unable to load report." }
                    }
                }.addOnFailureListener { text = it.localizedMessage ?: "Unable to verify alert ownership." }
        }
    }
    AlertDialog(onDismissRequest = onClose,
        title = { Text("Sighting Report") },
        text = { Text(text, Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = onClose) { Text("Close") } })
}
