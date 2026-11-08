import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanderspectra.app.AlertSighting
import com.wanderspectra.app.ui.theme.PrimaryBlue
import com.wanderspectra.app.ui.theme.Salsa
import com.wanderspectra.app.ui.theme.SecondaryBlue
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import com.wanderspectra.app.ui.theme.ButtonBlue
import com.wanderspectra.app.ui.theme.CardCream
import com.wanderspectra.app.ui.theme.EmergencyRed
import com.wanderspectra.app.ui.theme.TanSongbird
import android.location.Geocoder
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.material3.Surface
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition

@Composable
fun SightingReportDetailScreen(
    sighting: AlertSighting,
    onBack: () -> Unit
) {
    val dateFormatter = SimpleDateFormat(
        "MMMM d, yyyy 'at' h:mm a",
        Locale.getDefault()
    )

    val context = LocalContext.current

    var sightingAddress by remember(
        sighting.latitude,
        sighting.longitude
    ) {
        mutableStateOf("Finding nearest address...")
    }

    LaunchedEffect(sighting.latitude, sighting.longitude) {
        sightingAddress = withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                Geocoder(context, Locale.getDefault())
                    .getFromLocation(
                        sighting.latitude,
                        sighting.longitude,
                        1
                    )
                    ?.firstOrNull()
                    ?.getAddressLine(0)
                    ?: "Street address unavailable"
            } catch (e: Exception) {
                "Street address unavailable"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .background(
                color = CardCream,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = PrimaryBlue,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Sighting Report",
            fontFamily = TanSongbird,
            fontSize = 18.sp,
            color = EmergencyRed
        )

        Spacer(modifier = Modifier.height(24.dp))

        @Composable
        fun ReportField(label: String, value: String) {
            Text(
                text = label,
                fontFamily = Salsa,
                fontSize = 18.sp,
                color = EmergencyRed
            )

            Text(
                text = value,
                fontFamily = Salsa,
                fontSize = 18.sp,
                color = PrimaryBlue,
                modifier = Modifier.padding(start = 14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        ReportField(
            "Date and Time:",
            sighting.sightingTime?.toDate()?.let {
                dateFormatter.format(it)
            } ?: "Unknown"
        )

        ReportField(
            "Direction:",
            sighting.direction.ifBlank { "Unknown" }
        )

        ReportField(
            "Location:",
            sightingAddress
        )

        val sightingLocation = LatLng(
            sighting.latitude,
            sighting.longitude
        )

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                sightingLocation,
                16f
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, PrimaryBlue)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = sightingLocation),
                    title = "Reported Sighting",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        ReportField(
            "Details:",
            sighting.details.ifBlank { "None provided" }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(54.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonBlue
            ),
            border = BorderStroke(1.dp, SecondaryBlue)
        ) {
            Text(
                text = "Back",
                fontFamily = Salsa,
                fontSize = 20.sp,
                color = SecondaryBlue
            )
        }
    }
}
