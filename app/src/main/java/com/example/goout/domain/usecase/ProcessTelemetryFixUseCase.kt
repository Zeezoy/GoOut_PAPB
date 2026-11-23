package com.example.goout.domain.usecase

import com.example.goout.core.common.CorridorConfig
import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.LocationFix
import com.example.goout.domain.model.TelemetryEvent
import javax.inject.Inject

/** Alasan sebuah fix telemetri tidak dijadikan event. */
enum class TelemetryRejection {
    NOT_AUTHENTICATED, NO_SPEED, INVALID_SPEED, OUTSIDE_CORRIDOR, TOO_FREQUENT, POOR_ACCURACY,
}

sealed interface TelemetryOutcome {
    data class Accepted(val event: TelemetryEvent) : TelemetryOutcome
    data class Rejected(val reason: TelemetryRejection) : TelemetryOutcome
}

const val MPS_TO_KMH = 3.6

/**
 * FUNGSI 3 - Pemrosesan telemetri pasif (logika murni, mudah dites).
 * [OWNER: Keenan] [FUNGSI 3]
 *
 * Urutan yang diharapkan test: login -> kecepatan ada -> kecepatan tidak negatif
 * -> geofence (termasuk akurasi) -> interval minimal -> event diterima.
 * km/jam = m/s * 3,6.
 *
 * Test spesifikasi: ProcessTelemetryFixTest (8 test).
 */
class ProcessTelemetryFixUseCase @Inject constructor(
    private val validateGeofence: ValidateGeofenceUseCase,
) {
    /** Waktu event diterima terakhir, selama proses hidup. */
    @Volatile
    private var lastAcceptedAt: Long = 0L

    fun reset() { lastAcceptedAt = 0L }

    operator fun invoke(
        userId: String?,
        fix: LocationFix,
        corridors: List<Corridor>,
        now: Long = System.currentTimeMillis(),
        minIntervalMillis: Long = CorridorConfig.TELEMETRY_MIN_INTERVAL_MILLIS,
        source: String = "gps",
    ): TelemetryOutcome {
        // TODO(Keenan): implementasikan urutan validasi di atas.
        return TelemetryOutcome.Rejected(TelemetryRejection.NO_SPEED)
    }
}
