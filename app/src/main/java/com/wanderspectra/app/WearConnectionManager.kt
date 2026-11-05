package com.wanderspectra.app

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject
import com.google.android.gms.wearable.PutDataMapRequest

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
    private const val PLAY_SOUND_PATH = "/wanderspectra/play_sound"
    private const val STOP_SOUND_PATH = "/wanderspectra/stop_sound"
    private const val SAFEZONE_PATH = "/wanderspectra/safezone"
    private const val SAFEZONE_DELETE_PATH = "/wanderspectra/safezone/delete"

    private fun safeLogE(tag: String, msg: String, throwable: Throwable? = null) {
        try {
            Log.e(tag, msg, throwable)
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

    fun getWearableForChild(
        context: Context,
        childId: String,
        onResult: (Node?) -> Unit
    ) {
        if (childId.isBlank()) {
            onResult(null)
            return
        }

        WearableRepository().getWearable(
            childId = childId,
            onSuccess = { wearable ->

                val deviceId = wearable?.deviceId

                if (deviceId.isNullOrBlank()) {
                    onResult(null)
                } else {
                    getAvailableWearables(context) { nodes ->

                        val matchingNode = nodes.firstOrNull {
                            it.id == deviceId
                        }

                        onResult(matchingNode)
                    }
                }
            },
            onFailure = { exception ->
                safeLogE(
                    TAG,
                    "Failed to retrieve wearable for child $childId",
                    exception
                )

                onResult(null)
            }
        )
    }

    fun syncChildSafeZone(
        context: Context,
        childId: String,
        safeZone: SafeZone,
        enabled: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        getWearableForChild(context, childId) { node ->

            if (node == null) {
                onResult(false)
                return@getWearableForChild
            }

            val childZone = safeZone.copy(
                childId = childId,
                monitoringEnabled = enabled
            )

            sendSafeZoneToWearable(
                context = context,
                nodeId = node.id,
                safeZone = childZone,
                childId = childId,
                onResult = onResult
            )
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
        childName: String,
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
                JSONObject().apply {
                    put("childName", childName)
                }.toString().toByteArray(Charsets.UTF_8)
            )
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
        sendConnectionRequest(
            context = context,
            nodeId = nodeId,
            childName = ""
        ) { connected, battery, _, _ ->
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

    fun playSoundOnWearable(
        context: Context,
        nodeId: String,
        onResult: (Boolean) -> Unit
    ) {
        val messageClient = Wearable.getMessageClient(context)
        messageClient
            .sendMessage(
                nodeId,
                PLAY_SOUND_PATH,
                "Play sound request".toByteArray(Charsets.UTF_8)
            )
            .addOnSuccessListener {

                onResult(true)
            }
            .addOnFailureListener { exception ->
                safeLogE(TAG, "Failed to send play sound message to $nodeId", exception)
                onResult(false)
            }
    }

    fun stopSoundOnWearable(
        context: Context,
        nodeId: String,
        onResult: (Boolean) -> Unit
    ) {
        val messageClient = Wearable.getMessageClient(context)

        messageClient
            .sendMessage(
                nodeId,
                STOP_SOUND_PATH,
                "Stop sound request".toByteArray(Charsets.UTF_8)
            )
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener { exception ->
                safeLogE(TAG, "Failed to send stop sound message to $nodeId", exception)
                onResult(false)
            }
    }

    fun sendSafeZoneToWearable(
        context: Context,
        nodeId: String,
        safeZone: SafeZone,
        childId: String = "",
        onResult: (Boolean) -> Unit
    ) {
        val payload = JSONObject().apply {
            put("id", safeZone.id)
            put("childId", childId)
            put("lat", safeZone.lat)
            put("lng", safeZone.lng)
            put("radius", safeZone.radius)
            put("monitoringEnabled", safeZone.monitoringEnabled)
        }

        Wearable.getMessageClient(context)
            .sendMessage(
                nodeId,
                SAFEZONE_PATH,
                payload.toString().toByteArray(Charsets.UTF_8)
            )
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener { exception ->
                safeLogE(
                    TAG,
                    "Failed to send Safe Zone to wearable $nodeId",
                    exception
                )
                onResult(false)
            }
    }
    fun sendSafeZoneDeleteToWearable(
        context: Context,
        nodeId: String,
        zoneId: String,
        onResult: (Boolean) -> Unit
    ) {
        if (zoneId.isBlank()) {
            onResult(false)
            return
        }

        val payload = JSONObject().apply {
            put("id", zoneId)
        }

        Wearable.getMessageClient(context)
            .sendMessage(
                nodeId,
                SAFEZONE_DELETE_PATH,
                payload.toString().toByteArray(Charsets.UTF_8)
            )
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener { exception ->
                safeLogE(
                    TAG,
                    "Failed to send Safe Zone deletion message to $nodeId",
                    exception
                )
                onResult(false)
            }
    }
    fun publishSafeZoneDeletion(
        context: Context,
        zoneId: String,
        onResult: (Boolean) -> Unit
    ) {
        if (zoneId.isBlank()) {
            onResult(false)
            return
        }

        val dataRequest = PutDataMapRequest.create(
            "/wanderspectra/safezone/state"
        ).apply {
            dataMap.putBoolean("deleted", true)
            dataMap.putString("zoneId", zoneId)
            dataMap.putLong("updatedAt", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()

        Wearable.getDataClient(context)
            .putDataItem(dataRequest)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener { exception ->
                safeLogE(
                    TAG,
                    "Failed to publish Safe Zone deletion state",
                    exception
                )
                onResult(false)
            }
    }
    fun publishSafeZoneActive(
        context: Context,
        zoneId: String,
        onResult: (Boolean) -> Unit
    ) {
        if (zoneId.isBlank()) {
            onResult(false)
            return
        }

        val dataRequest = PutDataMapRequest.create(
            "/wanderspectra/safezone/state"
        ).apply {
            dataMap.putBoolean("deleted", false)
            dataMap.putString("zoneId", zoneId)
            dataMap.putLong("updatedAt", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()

        Wearable.getDataClient(context)
            .putDataItem(dataRequest)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener { exception ->
                safeLogE(TAG, "Failed to publish Safe Zone active state", exception)
                onResult(false)
            }
    }
}
