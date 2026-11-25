package com.example.goout.domain.usecase

import com.example.goout.core.common.CorridorConfig
import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.LocationFix
import com.example.goout.domain.model.TelemetryEvent
import javax.inject.Inject
import com.example.goout.domain.model.GeoPoint
import java.util.UUID

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
        if (userId.isNullOrBlank()) return TelemetryOutcome.Rejected(TelemetryRejection.NOT_AUTHENTICATED)
        val speedMps = fix.speedMetersPerSecond
            ?: return TelemetryOutcome.Rejected(TelemetryRejection.NO_SPEED)
        if (speedMps.isNaN() || speedMps < 0f) {
            return TelemetryOutcome.Rejected(TelemetryRejection.INVALID_SPEED)
        }
        val speedKmh = speedMps * MPS_TO_KMH // km/jam = m/s * 3,6
        val geofence = validateGeofence(
            GeoPoint(fix.latitude, fix.longitude),
            corridors,
            fix.accuracyMeters,
        )
        if (!geofence.isValid || geofence.corridorId == null) {
            return TelemetryOutcome.Rejected(
                if (geofence.reason == com.example.goout.domain.model.GeofenceReason.POOR_ACCURACY) {
                    TelemetryRejection.POOR_ACCURACY
                } else {
                    TelemetryRejection.OUTSIDE_CORRIDOR
                },
            )
        }
        if (now - lastAcceptedAt < minIntervalMillis) {
            return TelemetryOutcome.Rejected(TelemetryRejection.TOO_FREQUENT)
        }
        lastAcceptedAt = now
        return TelemetryOutcome.Accepted(
            TelemetryEvent(
                telemetryId = UUID.randomUUID().toString(),
                userId = userId,
                corridorId = geofence.corridorId,
                latitude = fix.latitude,
                longitude = fix.longitude,
                speedKmh = speedKmh,
                timestamp = now,
                source = source,
            ),
        )
    }
}