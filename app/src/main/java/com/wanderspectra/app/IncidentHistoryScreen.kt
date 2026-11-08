package com.wanderspectra.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.ui.theme.BackgroundCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.TanSongbird


@Composable
fun IncidentHistoryScreen(
    childId: String,
    onViewReport: (IncidentRecord) -> Unit,
    childName: String = "",
    modifier: Modifier = Modifier
) {
    val repository = remember { IncidentRepository() }
    var refreshVersion by remember(childId) { mutableStateOf(0) }
    var loadState by remember(childId) { mutableStateOf<IncidentHistoryLoadState>(IncidentHistoryLoadState.Loading) }

    DisposableEffect(childId, refreshVersion) {
        var acceptingResults = true

        if (childId.isBlank()) {
            loadState = IncidentHistoryLoadState.NoChildSelected
        } else {
            loadState = IncidentHistoryLoadState.Loading

            repository.getIncidentsForChild(
                childId = childId,

                onSuccess = { incidents ->
                    if (acceptingResults) {
                        loadState =
                            IncidentHistoryLoadState.Success(incidents)
                    }
                },

                onFailure = { exception ->
                    if (acceptingResults) {
                        loadState = IncidentHistoryLoadState.Failure(
                            exception.localizedMessage
                                ?: "Unable to retrieve incident history."
                        )
                    }
                }
            )
        }

        onDispose {
            acceptingResults = false
        }
    }

    when (val state = loadState) {

        IncidentHistoryLoadState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(BackgroundCream),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryBlue
                )
            }
        }

        IncidentHistoryLoadState.NoChildSelected -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(BackgroundCream)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Incident History",
                    color = EmergencyRed,
                    fontFamily = TanSongbird,
                    fontSize = 20.sp
                )
            }
        }

        is IncidentHistoryLoadState.Success -> {
            IncidentHistoryContent(
                incidents = state.incidents,
                childName = childName,
                onViewReport = onViewReport,
                modifier = modifier
            )
        }

        is IncidentHistoryLoadState.Failure -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(BackgroundCream)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unable to Load Incident History",
                    color = PrimaryBlue,
                    fontFamily = Salsa,
                    fontSize = 22.sp
                )

                Text(
                    text = state.message,
                    color = PrimaryBlue,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                TextButton(
                    onClick = {
                        refreshVersion += 1
                    }
                ) {
                    Text(
                        text = "Retry",
                        color = PrimaryBlue,
                        fontFamily = Salsa
                    )
                }
            }
        }
    }
}
private sealed class IncidentHistoryLoadState {

    object Loading : IncidentHistoryLoadState()

    object NoChildSelected : IncidentHistoryLoadState()

    data class Success(
        val incidents: List<IncidentRecord>
    ) : IncidentHistoryLoadState()

    data class Failure(
        val message: String
    ) : IncidentHistoryLoadState()
}
