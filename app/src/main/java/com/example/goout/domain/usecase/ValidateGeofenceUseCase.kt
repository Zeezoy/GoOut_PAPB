package com.example.goout.domain.usecase

import com.example.goout.core.utils.GeoUtils
import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.GeofenceReason
import com.example.goout.domain.model.GeofenceResult
import com.example.goout.domain.model.GeoPoint
import javax.inject.Inject

/**
 * FUNGSI 2 - Validasi geofence koridor.
 * [OWNER: Radit] [FUNGSI 2]
 *
 * Data (telemetri atau laporan) valid hanya bila perangkat berada dalam
 * [MAX_DISTANCE_METERS] dari koridor terdekat. Jarak adalah jarak terpendek
 * titik ke ruas polyline (bukan hanya ke titik ujung).
 *
 * Test spesifikasi: GeofenceValidatorTest (9 test).
 */
class GeofenceValidator @Inject constructor() {

    companion object {
        const val MAX_DISTANCE_METERS = 100.0

        /** Fix dengan akurasi horizontal lebih buruk dari ini ditolak. */
        const val MAX_ACCURACY_METERS = 50.0
    }

    fun validate(location: GeoPoint, corridors: List<Corridor>, accuracyMeters: Float? = null): GeofenceResult {
        // 1. koordinat valid?
        if (!location.isValid) return GeofenceResult(false, null, null, GeofenceReason.INVALID_COORDINATES)
        // 2. akurasi cukup?
        if (accuracyMeters != null && accuracyMeters > MAX_ACCURACY_METERS) return GeofenceResult(false, null, null, GeofenceReason.POOR_ACCURACY)
        // 3. koridor yang polyline-nya tidak kosong
        val usable = corridors.filter { it.polyline.isNotEmpty() }
        if (usable.isEmpty()) return GeofenceResult(false, null, null, GeofenceReason.NO_CORRIDOR_DATA)
        // 4. cari yang terdekat
        var idTerdekat: String? = null
        var jarakTerdekat = Double.MAX_VALUE
        for (koridor in usable) {
            val jarak = GeoUtils.distanceToPolylineMeters(location, koridor.polyline)
            if (jarak < jarakTerdekat) { jarakTerdekat = jarak; idTerdekat = koridor.corridorId }
        }
        // 5. putuskan (<= batas; epsilon menyerap galat pembulatan di tepat 100 m)
        return if (jarakTerdekat <= MAX_DISTANCE_METERS + 1e-6) {
            GeofenceResult(true, idTerdekat, jarakTerdekat, GeofenceReason.WITHIN_CORRIDOR)
        } else {
            GeofenceResult(false, idTerdekat, jarakTerdekat, GeofenceReason.OUTSIDE_CORRIDOR)
        }
    }
}

class ValidateGeofenceUseCase @Inject constructor(private val validator: GeofenceValidator) {
    operator fun invoke(
        location: GeoPoint,
        corridors: List<Corridor>,
        accuracyMeters: Float? = null,
    ): GeofenceResult = validator.validate(location, corridors, accuracyMeters)
}
