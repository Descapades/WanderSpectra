package com.wanderspectra.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** System connectivity only: a validated network does not prove a Firebase request succeeded. */
@Composable
fun rememberPhoneInternetAvailable(): State<Boolean?> {
    val context = LocalContext.current.applicationContext
    val state = remember(context) { mutableStateOf<Boolean?>(null) }
    DisposableEffect(context) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val handler = Handler(Looper.getMainLooper())
        var disposed = false
        fun publish(value: Boolean?) {
            handler.post { if (!disposed) state.value = value }
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                publish(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            }
            override fun onLost(network: Network) { publish(false) }
        }
        var registered = false
        try {
            manager.registerDefaultNetworkCallback(callback)
            registered = true
            val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
            state.value = capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (_: SecurityException) {
            state.value = null
        }
        onDispose {
            disposed = true
            if (registered) manager.unregisterNetworkCallback(callback)
        }
    }
    return state
}

@Composable
fun PhoneConnectivityBanner() {
    val online by rememberPhoneInternetAvailable()
    var hadOutage by remember { mutableStateOf(false) }
    var restored by remember { mutableStateOf(false) }
    LaunchedEffect(online) {
        if (online == false) {
            hadOutage = true
            restored = false
        } else if (online == true && hadOutage) {
            restored = true
            delay(5_000L)
            restored = false
            hadOutage = false
        }
    }
    val message = when {
        online == false -> "Phone offline or internet unavailable. Saved information may be outdated; updates may be delayed."
        restored -> "Internet connection restored. Reopen a screen or use Retry to refresh."
        else -> null
    }
    message?.let {
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.secondaryContainer) {
            Text(it, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
