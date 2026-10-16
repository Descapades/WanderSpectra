package com.wanderspectra.app

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject

data class WearableLocationResult(
    val connected: Boolean,
    val batteryLevel: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object WearConnectionManager {

    private const val TAG = "WearConnection"
    private const val CONNECT_PATH = "/wanderspectra/connect"
    private const val CONNECTED_PATH = "/wanderspectra/connected"
    private const val LOCATION_PATH = "/wanderspectra/location"
    private const val LOCATION_RESPONSE_PATH = "/wanderspectra/location_response"
    private const val ELOPEMENT_PATH = "/wanderspectra/elopement"
    private const val SAFEZONE_PATH = "/wanderspectra/safezone_update"

    private fun safeLogE(tag: String, msg: String, throwable: Throwable? = null) {
        try {
            Log.e(tag, msg, throwable)
        } catch (_: Throwable) {
            // Ignored in plain JVM unit test environment
        }
    }

    private fun safeLogD(tag: String, msg: String) {
        try {
            Log.d(tag, msg)
        } catch (_: Throwable) {
            // Ignored in plain JVM unit test environment
        }
    }

    fun getAvailableWearables(
        context: Context,
        onResult: (List<Node>) -> Unit
    ) {
        Wearable.getNodeClient(context)
            .connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.forEach { node ->
                    safeLogD(
                        TAG,
                        "Available Wear device: ${node.displayName} (${node.id})"
                    )
                }

                onResult(nodes)
            }
            .addOnFailureListener { exception ->
                safeLogE(
                    TAG,
                    "Failed to find Wear OS devices",
                    exception
                )

                onResult(emptyList())
            }
    }

    fun parseLocationResponse(rawString: String): WearableLocationResult {
        val trimmed = rawString.trim()
        if (trimmed.startsWith("{")) {
            return try {
                val json = JSONObject(trimmed)
                val battery = json.optInt("batteryLevel", -1)
                val lat = if (json.has("latitude") && !json.isNull("latitude")) json.getDouble("latitude") else null
                val lng = if (json.has("longitude") && !json.isNull("longitude")) json.getDouble("longitude") else null
                val time = json.optLong("timestamp", System.currentTimeMillis())

                if (battery == -1) {
                    parseJsonFallback(trimmed)
                } else {
                    WearableLocationResult(
                        connected = true,
                        batteryLevel = battery,
                        latitude = lat,
                        longitude = lng,
                        timestamp = time
                    )
                }
            } catch (_: Throwable) {
                parseJsonFallback(trimmed)
            }
        } else {
            val battery = trimmed.toIntOrNull() ?: -1
            return WearableLocationResult(
                connected = true,
                batteryLevel = battery
            )
        }
    }

    private fun parseJsonFallback(jsonString: String): WearableLocationResult {
        val batteryRegex = """"batteryLevel"\s*:\s*(-?\d+)""".toRegex()
        val latRegex = """"latitude"\s*:\s*(-?\d+(?:\.\d+)?)""".toRegex()
        val lngRegex = """"longitude"\s*:\s*(-?\d+(?:\.\d+)?)""".toRegex()
        val timeRegex = """"timestamp"\s*:\s*(\d+)""".toRegex()

        val battery = batteryRegex.find(jsonString)?.groupValues?.get(1)?.toIntOrNull() ?: -1
        val lat = latRegex.find(jsonString)?.groupValues?.get(1)?.toDoubleOrNull()
        val lng = lngRegex.find(jsonString)?.groupValues?.get(1)?.toDoubleOrNull()
        val time = timeRegex.find(jsonString)?.groupValues?.get(1)?.toLongOrNull() ?: System.currentTimeMillis()

        return WearableLocationResult(
            connected = true,
            batteryLevel = battery,
            latitude = lat,
            longitude = lng,
            timestamp = time
        )
    }

    fun sendConnectionRequest(
        context: Context,
        nodeId: String,
        onResult: (Boolean, Int, Double?, Double?) -> Unit
    ) {
        val messageClient = Wearable.getMessageClient(context)

        lateinit var responseListener: MessageClient.OnMessageReceivedListener

        responseListener =
            MessageClient.OnMessageReceivedListener { messageEvent: MessageEvent ->

                if (
                    (messageEvent.path == CONNECTED_PATH || messageEvent.path == LOCATION_RESPONSE_PATH) &&
                    messageEvent.sourceNodeId == nodeId
                ) {
                    val rawPayload = messageEvent.data.toString(Charsets.UTF_8)
                    val result = parseLocationResponse(rawPayload)

                    safeLogD(
                        TAG,
                        "WanderSpectra connection confirmed. Battery: ${result.batteryLevel}%, Lat: ${result.latitude}, Lng: ${result.longitude}"
                    )

                    messageClient.removeListener(responseListener)

                    onResult(
                        true,
                        result.batteryLevel,
                        result.latitude,
                        result.longitude
                    )
                }
            }

        messageClient.addListener(responseListener)

        messageClient
            .sendMessage(
                nodeId,
                CONNECT_PATH,
                "WanderSpectra connection request".toByteArray()
            )
            .addOnSuccessListener {
                safeLogD(
                    TAG,
                    "Connection request sent. Waiting for Wear response."
                )
            }
            .addOnFailureListener { exception ->
                safeLogE(
                    TAG,
                    "Failed to send connection request",
                    exception
                )

                messageClient.removeListener(responseListener)

                onResult(
                    false,
                    -1,
                    null,
                    null
                )
            }
    }

    // Overload for backward compatibility
    fun sendConnectionRequest(
        context: Context,
        nodeId: String,
        onResult: (Boolean, Int) -> Unit
    ) {
        sendConnectionRequest(context, nodeId) { connected, battery, _, _ ->
            onResult(connected, battery)
        }
    }

    fun requestWearableLocation(
        context: Context,
        nodeId: String,
        onResult: (WearableLocationResult) -> Unit
    ) {
        val messageClient = Wearable.getMessageClient(context)

        lateinit var responseListener: MessageClient.OnMessageReceivedListener

        responseListener =
            MessageClient.OnMessageReceivedListener { messageEvent: MessageEvent ->

                if (
                    (messageEvent.path == LOCATION_RESPONSE_PATH || messageEvent.path == CONNECTED_PATH) &&
                    messageEvent.sourceNodeId == nodeId
                ) {
                    val rawPayload = messageEvent.data.toString(Charsets.UTF_8)
                    val result = parseLocationResponse(rawPayload)

                    safeLogD(
                        TAG,
                        "Location response received from Wear device: $result"
                    )

                    messageClient.removeListener(responseListener)

                    onResult(result)
                }
            }

        messageClient.addListener(responseListener)

        messageClient
            .sendMessage(
                nodeId,
                LOCATION_PATH,
                "Request location".toByteArray()
            )
            .addOnSuccessListener {
                safeLogD(
                    TAG,
                    "Location request sent to Wear device $nodeId"
                )
            }
            .addOnFailureListener { exception ->
                safeLogE(
                    TAG,
                    "Failed to send location request to Wear device $nodeId",
                    exception
                )

                messageClient.removeListener(responseListener)

                onResult(
                    WearableLocationResult(
                        connected = false,
                        batteryLevel = -1
                    )
                )
            }
    }

    fun sendElopementAlert(
        context: Context,
        isElopementActive: Boolean,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val messageClient = Wearable.getMessageClient(context)
        val nodeClient = Wearable.getNodeClient(context)

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                onComplete?.invoke(false)
                return@addOnSuccessListener
            }

            val payload = if (isElopementActive) "true" else "false"
            var sentCount = 0

            nodes.forEach { node ->
                messageClient.sendMessage(node.id, ELOPEMENT_PATH, payload.toByteArray())
                    .addOnSuccessListener {
                        sentCount++
                        if (sentCount == nodes.size) {
                            onComplete?.invoke(true)
                        }
                    }
                    .addOnFailureListener {
                        onComplete?.invoke(false)
                    }
            }
        }.addOnFailureListener {
            onComplete?.invoke(false)
        }
    }

    fun sendSafeZoneUpdate(
        context: Context,
        safeZone: SafeZone,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val messageClient = Wearable.getMessageClient(context)
        val nodeClient = Wearable.getNodeClient(context)

        val json = JSONObject().apply {
            put("lat", safeZone.lat)
            put("lng", safeZone.lng)
            put("radius", safeZone.radius)
            put("monitoringEnabled", safeZone.monitoringEnabled)
        }
        val payload = json.toString().toByteArray(Charsets.UTF_8)

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                onComplete?.invoke(false)
                return@addOnSuccessListener
            }

            var sentCount = 0
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, SAFEZONE_PATH, payload)
                    .addOnSuccessListener {
                        sentCount++
                        if (sentCount == nodes.size) {
                            onComplete?.invoke(true)
                        }
                    }
                    .addOnFailureListener {
                        onComplete?.invoke(false)
                    }
            }
        }.addOnFailureListener {
            onComplete?.invoke(false)
        }
    }
}
