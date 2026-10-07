package com.wanderspectra.app

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

object WearConnectionManager {

    private const val TAG = "WearConnection"
    private const val CONNECT_PATH = "/wanderspectra/connect"
    private const val CONNECTED_PATH = "/wanderspectra/connected"

    fun getAvailableWearables(
        context: Context,
        onResult: (List<Node>) -> Unit
    ) {
        Wearable.getNodeClient(context)
            .connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.forEach { node ->
                    Log.d(
                        TAG,
                        "Available Wear device: ${node.displayName} (${node.id})"
                    )
                }

                onResult(nodes)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to find Wear OS devices",
                    exception
                )

                onResult(emptyList())
            }
    }

    fun sendConnectionRequest(
        context: Context,
        nodeId: String,
        onResult: (Boolean, Int) -> Unit
    ) {
        val messageClient = Wearable.getMessageClient(context)

        lateinit var responseListener: MessageClient.OnMessageReceivedListener

        responseListener =
            MessageClient.OnMessageReceivedListener { messageEvent: MessageEvent ->

                if (
                    messageEvent.path == CONNECTED_PATH &&
                    messageEvent.sourceNodeId == nodeId
                ) {
                    val batteryLevel = messageEvent.data
                        .toString(Charsets.UTF_8)
                        .toIntOrNull() ?: -1

                    Log.d(
                        TAG,
                        "WanderSpectra connection confirmed. Battery: $batteryLevel%"
                    )

                    messageClient.removeListener(responseListener)

                    onResult(
                        true,
                        batteryLevel
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
                Log.d(
                    TAG,
                    "Connection request sent. Waiting for Wear response."
                )
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to send connection request",
                    exception
                )

                messageClient.removeListener(responseListener)

                onResult(
                    false,
                    -1
                )
            }
    }
}
