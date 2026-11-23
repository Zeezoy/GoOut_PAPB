package com.example.goout.core.utils

import com.example.goout.domain.model.GeoPoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * FUNGSI 2 - Utilitas geodesi untuk validasi geofence.
 * [OWNER: Radit] [FUNGSI 2]
 *
 * Test spesifikasi: GeofenceValidatorTest dan CorridorConfigTest.
 */
object GeoUtils {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /** Jarak great-circle dua koordinat dalam meter (rumus Haversine). */
    fun haversineMeters(origin: GeoPoint, destination: GeoPoint): Double {
        val deltaLatRad = Math.toRadians(destination.latitude - origin.latitude)
        val deltaLonRad = Math.toRadians(destination.longitude - origin.longitude)

        val originLatRad = Math.toRadians(origin.latitude)
        val destLatRad = Math.toRadians(destination.latitude)

        val halfDeltaLatSin = sin(deltaLatRad * 0.5)
        val halfDeltaLonSin = sin(deltaLonRad * 0.5)

        val haversineVal = (halfDeltaLatSin * halfDeltaLatSin) +
                cos(originLatRad) * cos(destLatRad) * (halfDeltaLonSin * halfDeltaLonSin)

        val centralAngle = 2.0 * asin(sqrt(haversineVal.coerceIn(0.0, 1.0)))
        return EARTH_RADIUS_METERS * centralAngle
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
