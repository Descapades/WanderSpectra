package com.wanderspectra.app

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

object WearConnectionManager {

    private const val TAG = "WearConnection"
    private const val CONNECT_PATH = "/wanderspectra/connect"

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
        onResult: (Boolean) -> Unit
    ) {
        Wearable.getMessageClient(context)
            .sendMessage(
                nodeId,
                CONNECT_PATH,
                "WanderSpectra connection request".toByteArray()
            )
            .addOnSuccessListener {
                Log.d(TAG, "Connection request sent to Wear device")
                onResult(true)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to send connection request",
                    exception
                )

                onResult(false)
            }
    }
}

