package com.example.goout.domain.usecase

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

    fun validate(
        location: GeoPoint,
        corridors: List<Corridor>,
        accuracyMeters: Float? = null,
    ): GeofenceResult {
        // TODO(Radit): cek koordinat, akurasi, data koridor, lalu cari koridor terdekat
        // dan bandingkan jaraknya dengan MAX_DISTANCE_METERS (<= 100 m dianggap valid).
        return GeofenceResult(false, null, null, GeofenceReason.NO_CORRIDOR_DATA)
    }
}

class ValidateGeofenceUseCase @Inject constructor(private val validator: GeofenceValidator) {
    operator fun invoke(
        location: GeoPoint,
        corridors: List<Corridor>,
        accuracyMeters: Float? = null,
    ): GeofenceResult = validator.validate(location, corridors, accuracyMeters)
}
