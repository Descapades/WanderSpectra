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
    modifier: Modifier = Modifier,
    loadIncidents: ((String, (List<IncidentRecord>) -> Unit, (Exception) -> Unit) -> Unit)? = null
) {
    val repository = remember { IncidentRepository() }
    var refreshVersion by remember(childId) { mutableStateOf(0) }
    var loadState by remember(childId) { mutableStateOf<IncidentHistoryLoadState>(IncidentHistoryLoadState.Loading) }

    DisposableEffect(childId, refreshVersion, loadIncidents) {
        var acceptingResults = true

        if (childId.isBlank()) {
            loadState = IncidentHistoryLoadState.NoChildSelected
        } else {
            loadState = IncidentHistoryLoadState.Loading

            val retrieve = loadIncidents ?: repository::getIncidentsForChild
            retrieve(
                childId,
                { incidents ->
                    if (acceptingResults) {
                        loadState =
                            IncidentHistoryLoadState.Success(incidents)
                    }
                },

                { exception ->
                    if (acceptingResults) {
                        loadState = IncidentHistoryLoadState.Failure(
                            caregiverDataError(exception)
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
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                DataStateFeedback(
                    title = "Incident History",
                    message = "Loading incidents...",
                    loading = true
                )
            }
        }

        IncidentHistoryLoadState.NoChildSelected -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                DataStateFeedback(
                    title = "Incident History",
                    message = "Select a child from the History menu to view their incidents."
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
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                DataStateFeedback(
                    title = "Unable to load Incident History",
                    message = state.message,
                    onRetry = { refreshVersion += 1 }
                )
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
