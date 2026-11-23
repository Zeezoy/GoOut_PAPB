package com.example.goout.core.utils

import com.example.goout.domain.model.GeoPoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.hypot
import kotlin.math.min

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
    fun distanceToSegmentMeters(target: GeoPoint, segStart: GeoPoint, segEnd: GeoPoint): Double {
        val midLatRad = Math.toRadians((segStart.latitude + segEnd.latitude) * 0.5)
        val cosMidLat = cos(midLatRad)

        // Konversi koordinat geografis ke bidang datar lokal dalam satuan meter
        fun toPlanarMeters(coord: GeoPoint): Pair<Double, Double> {
            val easting = Math.toRadians(coord.longitude) * EARTH_RADIUS_METERS * cosMidLat
            val northing = Math.toRadians(coord.latitude) * EARTH_RADIUS_METERS
            return Pair(easting, northing)
        }

        val (targetX, targetY) = toPlanarMeters(target)
        val (startX, startY) = toPlanarMeters(segStart)
        val (endX, endY) = toPlanarMeters(segEnd)

        val segVecX = endX - startX
        val segVecY = endY - startY
        val segLengthSquared = (segVecX * segVecX) + (segVecY * segVecY)

        val projectionRatio = if (segLengthSquared == 0.0) {
            0.0
        } else {
            val dotProduct = (targetX - startX) * segVecX + (targetY - startY) * segVecY
            (dotProduct / segLengthSquared).coerceIn(0.0, 1.0)
        }

        val closestX = startX + projectionRatio * segVecX
        val closestY = startY + projectionRatio * segVecY

        return hypot(targetX - closestX, targetY - closestY)
    }

    /** Jarak minimum (meter) dari [point] ke seluruh ruas pada [polyline]. */
    fun distanceToPolylineMeters(point: GeoPoint, polyline: List<GeoPoint>): Double {
        if (polyline.isEmpty()) return Double.MAX_VALUE
        if (polyline.size == 1) return haversineMeters(point, polyline[0])
        var terdekat = Double.MAX_VALUE            // mulai dari nilai sangat besar
        for (i in 0 until polyline.size - 1) {
            val jarak = distanceToSegmentMeters(point, polyline[i], polyline[i + 1])           // jarak ke ruas ke-i (titik i ke titik i+1)
            if (jarak < terdekat) terdekat = jarak
        }
        return terdekat
    }
}
