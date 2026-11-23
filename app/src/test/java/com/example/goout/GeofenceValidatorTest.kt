package com.example.goout

import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.model.GeofenceReason
import com.example.goout.domain.model.GeoPoint
import com.example.goout.domain.usecase.GeofenceValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** CORE FUNCTION 2 — geofence validation tests. */
class GeofenceValidatorTest {

    private val validator = GeofenceValidator()

    // Simple test corridor: a 222 m north-south segment on the equator-ish frame.
    private fun corridor(id: String, points: List<GeoPoint>) = Corridor(
        corridorId = id,
        corridorName = id,
        segmentLabel = "",
        polyline = points,
        densityStatus = DensityStatus.UNKNOWN,
        currentSpeed = null,
        lastUpdated = 0L,
    )

    private val earthRadius = 6_371_000.0

    /** Longitude offset (deg) that corresponds to [meters] east at latitude 0. */
    private fun lonOffsetForMeters(meters: Double): Double =
        Math.toDegrees(meters / earthRadius)

    private val segment = corridor(
        "c1",
        listOf(GeoPoint(-0.001, 0.0), GeoPoint(0.001, 0.0)),
    )

    @Test
    fun `point 50m from corridor is VALID`() {
        val p = GeoPoint(0.0, lonOffsetForMeters(50.0))
        val result = validator.validate(p, listOf(segment))
        assertTrue(result.isValid)
        assertEquals("c1", result.corridorId)
        assertEquals(50.0, result.distanceMeters!!, 0.5)
        assertEquals(GeofenceReason.WITHIN_CORRIDOR, result.reason)
    }

    @Test
    fun `point exactly 100m from corridor is VALID`() {
        val p = GeoPoint(0.0, lonOffsetForMeters(100.0))
        val result = validator.validate(p, listOf(segment))
        assertTrue(result.isValid)
        assertEquals(100.0, result.distanceMeters!!, 0.01)
    }

    @Test
    fun `point above 100m from corridor is INVALID`() {
        val p = GeoPoint(0.0, lonOffsetForMeters(150.0))
        val result = validator.validate(p, listOf(segment))
        assertFalse(result.isValid)
        assertEquals(GeofenceReason.OUTSIDE_CORRIDOR, result.reason)
        assertNotNull(result.distanceMeters)
    }

    @Test
    fun `nearest corridor is selected`() {
        val near = corridor("near", listOf(GeoPoint(-0.001, 0.0), GeoPoint(0.001, 0.0)))
        val far = corridor("far", listOf(GeoPoint(-0.001, 0.01), GeoPoint(0.001, 0.01)))
        val p = GeoPoint(0.0, lonOffsetForMeters(30.0))
        val result = validator.validate(p, listOf(far, near))
        assertTrue(result.isValid)
        assertEquals("near", result.corridorId)
    }

    @Test
    fun `distance is measured to the segment not its endpoints`() {
        // Point beside the middle of the segment: distance to the line is small
        // even though both endpoints are farther away.
        val longSegment = corridor("long", listOf(GeoPoint(-0.01, 0.0), GeoPoint(0.01, 0.0)))
        val p = GeoPoint(0.0, lonOffsetForMeters(40.0))
        val result = validator.validate(p, listOf(longSegment))
        assertTrue(result.isValid)
        assertEquals(40.0, result.distanceMeters!!, 0.5)
    }

    @Test
    fun `invalid coordinates are rejected`() {
        val result = validator.validate(GeoPoint(999.0, 999.0), listOf(segment))
        assertFalse(result.isValid)
        assertEquals(GeofenceReason.INVALID_COORDINATES, result.reason)
    }

    @Test
    fun `zero-zero coordinates are treated as missing location`() {
        val result = validator.validate(GeoPoint(0.0, 0.0), listOf(segment))
        assertFalse(result.isValid)
        assertEquals(GeofenceReason.INVALID_COORDINATES, result.reason)
    }

    @Test
    fun `poor GPS accuracy is rejected`() {
        val p = GeoPoint(0.0, lonOffsetForMeters(10.0))
        val result = validator.validate(p, listOf(segment), accuracyMeters = 120f)
        assertFalse(result.isValid)
        assertEquals(GeofenceReason.POOR_ACCURACY, result.reason)
    }

    @Test
    fun `empty corridor data is rejected`() {
        val p = GeoPoint(0.0001, lonOffsetForMeters(10.0))
        val result = validator.validate(p, emptyList())
        assertFalse(result.isValid)
        assertEquals(GeofenceReason.NO_CORRIDOR_DATA, result.reason)
    }
}
