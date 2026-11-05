package com.wanderspectra.app

import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.atomic.AtomicBoolean

/** Uses the confirmed child's saved wearable position without delaying an emergency indefinitely. */
class ElopementWorkflow {
    fun startConfirmedIncident(
        childId: String,
        caregiverId: String,
        onDone: (Boolean, String, String?) -> Unit
    ) {
        if (childId.isBlank() || caregiverId.isBlank() ||
            FirebaseAuth.getInstance().currentUser?.uid != caregiverId) {
            onDone(false, "Sign in and load a child profile before activating Elopement Mode.", null)
            return
        }
        val confirmedAt = System.currentTimeMillis()
        val handler = Handler(Looper.getMainLooper())
        val locationResolved = AtomicBoolean(false)
        var timeout: Runnable? = null
        fun save(wearable: Wearable?) {
            if (!locationResolved.compareAndSet(false, true)) return
            timeout?.let { handler.removeCallbacks(it) }
            if (FirebaseAuth.getInstance().currentUser?.uid != caregiverId) {
                onDone(false, "The caregiver session changed. Sign in again.", null)
                return
            }
            val lat = wearable?.latitude
            val lng = wearable?.longitude
            val valid = lat != null && lng != null && lat.isFinite() && lng.isFinite()
                && lat in -90.0..90.0 && lng in -180.0..180.0
            val incident = Incident(
                childId = childId,
                caregiverId = caregiverId,
                status = "open",
                startTime = confirmedAt,
                lastKnownLat = if (valid) lat else null,
                lastKnownLng = if (valid) lng else null
            )
            IncidentRepository().startIncident(incident) { ok, message, id ->
                val detail = if (ok) {
                    if (valid) "$message. Last reported wearable location recorded."
                    else "$message. Wearable location unavailable."
                } else message
                onDone(ok, detail, id)
            }
        }
        timeout = Runnable { save(null) }
        handler.postDelayed(timeout!!, 5_000L)
        WearableRepository().getWearable(
            childId = childId,
            onSuccess = { wearable -> handler.post { save(wearable) } },
            onFailure = { handler.post { save(null) } }
        )
    }
}
