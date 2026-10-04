package com.wanderspectra.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.wearable.Node
import androidx.compose.foundation.clickable
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.LaunchedEffect

private val WearableRed = Color(0xFFC34A5B)
private val WearableButtonBlue = Color(0xFFA9C8D9)
private val WearableCream = Color(0xFFEDEBE2)

@Composable
fun WearableSettingsContent() {

    var showDeviceSelection by remember {
        mutableStateOf(false)
    }

    if (showDeviceSelection) {
        ConnectWearableScreen(
            childName = "Jimothy",
            onDeviceConnected = {
                // Connected screen comes next.
            }
        )
    } else {
        WearableNotConnectedScreen(
            childName = "Jimothy",
            onConnectDevice = {
                showDeviceSelection = true
            }
        )
    }
}

@Composable
fun WearableNotConnectedScreen(
    childName: String,
    onConnectDevice: () -> Unit
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
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Not Connected",
                    color = WearableRed,
                    fontFamily = Salsa,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section title
            Text(
                text = "Connected Wearable",
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = "No wearable is currently\n" +
                        "connected to $childName’s\n" +
                        "profile. Please connect a\n" +
                        "device.",
                color = PrimaryBlue,
                fontFamily = Salsa,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Connect button
            Button(
                onClick = onConnectDevice,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(170.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
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
fun ConnectWearableScreen(
    childName: String,
    onDeviceConnected: () -> Unit
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
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Not Connected",
                    color = WearableRed,
                    fontFamily = Salsa,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Connect Wearable",
                color = PrimaryBlue,
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
                            }
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
                                color = PrimaryBlue,
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
                        ) { sent ->
                            if (sent) {
                                onDeviceConnected()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(170.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
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