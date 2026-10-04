package com.wanderspectra.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.wearable.Node
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import androidx.compose.foundation.BorderStroke

private val WearableRed = Color(0xFFC34A5B)
private val WearableButtonBlue = Color(0xFFA9C8D9)
private val WearableCream = Color(0xFFEDEBE2)

@Composable
fun WearableSettingsContent(
    child: ChildProfile
) {

    var showDeviceSelection by remember {
        mutableStateOf(false)
    }

    var isConnected by remember {
        mutableStateOf(false)
    }

    var connectedDeviceName by remember {
        mutableStateOf("")
    }

    var batteryLevel by remember {
        mutableStateOf(-1)
    }

    val wearableRepository = remember {
        WearableRepository()
    }

    val childName = child.preferredName
        .ifBlank { child.fullName }

    LaunchedEffect(child.childId) {
        wearableRepository.getWearable(
            childId = child.childId,
            onSuccess = { wearable ->

                android.util.Log.d(
                    "WearableFirestore",
                    "Retrieved wearable: $wearable"
                )

                if (wearable != null) {
                    connectedDeviceName = wearable.deviceName
                    batteryLevel = wearable.batteryLevel
                    isConnected = wearable.connected
                }
            },
            onFailure = { exception ->
                android.util.Log.e(
                    "WearableFirestore",
                    "Failed to retrieve wearable",
                    exception
                )
            }
        )
    }

    if (isConnected) {

        WearableConnectedScreen(
            childName = childName,
            deviceName = connectedDeviceName,
            batteryLevel = batteryLevel,
            onDisconnectDevice = {
                isConnected = false
                showDeviceSelection = false
                connectedDeviceName = ""
                batteryLevel = -1
            }
        )

    } else if (showDeviceSelection) {

        ConnectWearableScreen(
            childName = childName,
            onDeviceConnected = { deviceId, deviceName, battery ->

                // The Wear OS connection already succeeded,
                // so update the UI immediately.
                connectedDeviceName = deviceName
                batteryLevel = battery
                isConnected = true

                val wearable = Wearable(
                    deviceId = deviceId,
                    deviceName = deviceName,
                    deviceType = "Wear OS Device",
                    batteryLevel = battery,
                    connected = true,
                    lastSync = System.currentTimeMillis()
                )

                wearableRepository.saveWearable(
                    childId = child.childId,
                    wearable = wearable,
                    onSuccess = {
                        android.util.Log.d(
                            "WearableFirestore",
                            "Wearable saved successfully"
                        )
                    },
                    onFailure = { exception ->
                        android.util.Log.e(
                            "WearableFirestore",
                            "Failed to save wearable",
                            exception
                        )
                    }
                )
            }
        )

    } else {

        WearableNotConnectedScreen(
            childName = childName,
            onFindDevice = {
                showDeviceSelection = true
            }
        )
    }
}

@Composable
fun WearableNotConnectedScreen(
    childName: String,
    onFindDevice: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(
                color = WearableCream,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                color = PrimaryBlue,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // Child name
            Text(
                text = childName,
                color = WearableRed,
                fontFamily = Salsa,
                fontSize = 30.sp
            )

            // Connection status
            Row(
                modifier = Modifier.padding(
                    start = 20.dp,
                    top = 2.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "●",
                    color = WearableRed,
                    fontSize = 30.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Not Connected",
                    color = WearableRed,
                    fontFamily = Salsa,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Connected Wearable",
                color = SecondaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "No wearable is currently\n" +
                        "connected to $childName’s\n" +
                        "profile. Please connect a\n" +
                        "device.",
                modifier = Modifier.padding(start = 20.dp),
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onFindDevice,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(170.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = PrimaryBlue
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WearableButtonBlue,
                    contentColor = PrimaryBlue
                )
            ) {
                Text(
                    text = "Find Device",
                    fontFamily = Salsa,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
fun ConnectWearableScreen(
    childName: String,
    onDeviceConnected: (String, String, Int) -> Unit
) {
    val context = LocalContext.current

    var availableDevices by remember {
        mutableStateOf<List<Node>>(emptyList())
    }

    var selectedDevice by remember {
        mutableStateOf<Node?>(null)
    }

    LaunchedEffect(Unit) {
        WearConnectionManager.getAvailableWearables(context) { nodes ->
            availableDevices = nodes
            selectedDevice = nodes.firstOrNull()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(
                color = WearableCream,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                color = PrimaryBlue,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                text = childName,
                color = WearableRed,
                fontFamily = Salsa,
                fontSize = 30.sp
            )

            Row(
                modifier = Modifier.padding(
                    start = 20.dp,
                    top = 2.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "●",
                    color = WearableRed,
                    fontSize = 30.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Not Connected",
                    color = WearableRed,
                    fontFamily = Salsa,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Connect Wearable",
                color = SecondaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Available Devices:",
                color = WearableRed,
                fontFamily = Salsa,
                fontSize = 16.sp
            )

            if (availableDevices.isEmpty()) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Searching for Wear OS devices...",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 16.sp
                )

            } else {

                availableDevices.forEach { node ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedDevice = node
                            },
                        verticalAlignment = Alignment.Top
                    ) {

                        RadioButton(
                            selected = selectedDevice?.id == node.id,
                            onClick = {
                                selectedDevice = node
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF1C4B76)
                            )
                        )

                        Column(
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = node.displayName,
                                color = PrimaryBlue,
                                fontFamily = Salsa,
                                fontSize = 16.sp
                            )

                            Text(
                                text = "Wear OS Device",
                                color = SecondaryBlue,
                                fontFamily = Salsa,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                enabled = selectedDevice != null,
                onClick = {
                    selectedDevice?.let { node ->
                        WearConnectionManager.sendConnectionRequest(
                            context = context,
                            nodeId = node.id
                        ) { connected, battery ->
                            if (connected) {
                                onDeviceConnected(
                                    node.id,
                                    node.displayName,
                                    battery
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(170.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = PrimaryBlue
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WearableButtonBlue,
                    contentColor = PrimaryBlue
                )
            ) {
                Text(
                    text = "Connect Device",
                    fontFamily = Salsa,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
fun WearableConnectedScreen(
    childName: String,
    deviceName: String,
    batteryLevel: Int,
    onDisconnectDevice: () -> Unit
) {
    val connectedGreen = Color(0xFF8EDCB4)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(
                color = WearableCream,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                color = PrimaryBlue,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                text = childName,
                color = WearableRed,
                fontFamily = Salsa,
                fontSize = 30.sp
            )

            Row(
                modifier = Modifier.padding(
                    start = 20.dp,
                    top = 2.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "●",
                    color = connectedGreen,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Connected",
                    color = connectedGreen,
                    fontFamily = Salsa,
                    fontSize = 22.sp
                )
            }

            if (batteryLevel >= 0) {
                Text(
                    text = "Battery $batteryLevel%",
                    modifier = Modifier.padding(start = 20.dp),
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Connected Wearable",
                color = SecondaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Wear OS Device",
                modifier = Modifier.padding(start = 10.dp),
                color = SecondaryBlue,
                fontFamily = Salsa,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Device Name:",
                modifier = Modifier.padding(start = 20.dp),
                color = WearableRed,
                fontFamily = Salsa,
                fontSize = 16.sp
            )

            Text(
                text = deviceName,
                modifier = Modifier.padding(start = 20.dp),
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Last Sync:",
                modifier = Modifier.padding(start = 20.dp),
                color = WearableRed,
                fontFamily = Salsa,
                fontSize = 16.sp
            )

            Text(
                text = "Just Now",
                modifier = Modifier.padding(start = 20.dp),
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onDisconnectDevice,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(170.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFA72A3C)
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE2A0A2),
                    contentColor = Color(0xFFA72A3C)
                ),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 0.dp
                )
            ) {
                Text(
                    text = "Disconnect Device",
                    fontFamily = Salsa,
                    fontSize = 14.sp,
                    color = Color(0xFFA72A3C)
                )
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}