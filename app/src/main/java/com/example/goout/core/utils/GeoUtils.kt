package com.example.goout.core.utils

import com.example.goout.domain.model.GeoPoint

/**
 * FUNGSI 2 - Utilitas geodesi untuk validasi geofence.
 * [OWNER: Radit] [FUNGSI 2]
 *
 * Test spesifikasi: GeofenceValidatorTest dan CorridorConfigTest.
 */
object GeoUtils {

    /** Jarak great-circle dua koordinat dalam meter (rumus Haversine). */
    fun haversineMeters(a: GeoPoint, b: GeoPoint): Double {
        // TODO(Radit): implementasikan Haversine.
        return Double.MAX_VALUE
    }

    /** Jarak terpendek (meter) dari [point] ke ruas [start]-[end]. */
    fun distanceToSegmentMeters(point: GeoPoint, start: GeoPoint, end: GeoPoint): Double {
        // TODO(Radit): proyeksi lokal ke bidang datar, proyeksi titik dijepit ke ruas.
        return Double.MAX_VALUE
    }

    /** Jarak minimum (meter) dari [point] ke seluruh ruas pada [polyline]. */
    fun distanceToPolylineMeters(point: GeoPoint, polyline: List<GeoPoint>): Double {
        // TODO(Radit): ambil minimum dari semua ruas.
        return Double.MAX_VALUE
    }
}
