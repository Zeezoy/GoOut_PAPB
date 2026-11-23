package com.example.goout

import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.model.GeoPoint
import com.example.goout.domain.model.LocationFix
import com.example.goout.domain.usecase.GeofenceValidator
import com.example.goout.domain.usecase.MPS_TO_KMH
import com.example.goout.domain.usecase.ProcessTelemetryFixUseCase
import com.example.goout.domain.usecase.TelemetryOutcome
import com.example.goout.domain.usecase.TelemetryRejection
import com.example.goout.domain.usecase.ValidateGeofenceUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** CORE FUNCTION 3 — passive telemetry processing tests. */
class ProcessTelemetryFixTest {

    private lateinit var useCase: ProcessTelemetryFixUseCase

    private val corridor = Corridor(
        corridorId = "c1",
        corridorName = "c1",
        segmentLabel = "",
        polyline = listOf(GeoPoint(-0.001, 0.0), GeoPoint(0.001, 0.0)),
        densityStatus = DensityStatus.UNKNOWN,
        currentSpeed = null,
        lastUpdated = 0L,
    )
    private val corridors = listOf(corridor)

    private fun fix(
        lat: Double = 0.0001,
        lng: Double = 0.0,
        speedMps: Float? = 10f,
        accuracy: Float? = 10f,
    ) = LocationFix(lat, lng, speedMps, accuracy, timestamp = 1_000L)

    @Before
    fun setup() {
        useCase = ProcessTelemetryFixUseCase(ValidateGeofenceUseCase(GeofenceValidator()))
    }

    @Test
    fun `valid GPS fix is processed into a telemetry event`() {
        val outcome = useCase("user1", fix(), corridors, now = 100_000L)
        assertTrue(outcome is TelemetryOutcome.Accepted)
        val event = (outcome as TelemetryOutcome.Accepted).event
        assertEquals("user1", event.userId)
        assertEquals("c1", event.corridorId)
    }

    @Test
    fun `speed conversion uses kmh = mps times 3_6`() {
        val outcome = useCase("user1", fix(speedMps = 10f), corridors, now = 100_000L)
        val event = (outcome as TelemetryOutcome.Accepted).event
        assertEquals(36.0, event.speedKmh, 0.0001)
        assertEquals(3.6, MPS_TO_KMH, 0.0)
    }

    @Test
    fun `missing speed is rejected`() {
        val outcome = useCase("user1", fix(speedMps = null), corridors, now = 100_000L)
        assertEquals(TelemetryRejection.NO_SPEED, (outcome as TelemetryOutcome.Rejected).reason)
    }

    @Test
    fun `negative speed is rejected`() {
        val outcome = useCase("user1", fix(speedMps = -1f), corridors, now = 100_000L)
        assertEquals(TelemetryRejection.INVALID_SPEED, (outcome as TelemetryOutcome.Rejected).reason)
    }

    @Test
    fun `location outside corridor is rejected`() {
        val outcome = useCase("user1", fix(lat = 1.0, lng = 1.0), corridors, now = 100_000L)
        assertEquals(TelemetryRejection.OUTSIDE_CORRIDOR, (outcome as TelemetryOutcome.Rejected).reason)
    }

    @Test
    fun `minimum submission interval is respected`() {
        val first = useCase("user1", fix(), corridors, now = 100_000L, minIntervalMillis = 10_000L)
        assertTrue(first is TelemetryOutcome.Accepted)

        val tooSoon = useCase("user1", fix(), corridors, now = 105_000L, minIntervalMillis = 10_000L)
        assertEquals(TelemetryRejection.TOO_FREQUENT, (tooSoon as TelemetryOutcome.Rejected).reason)

        val afterInterval = useCase("user1", fix(), corridors, now = 110_000L, minIntervalMillis = 10_000L)
        assertTrue(afterInterval is TelemetryOutcome.Accepted)
    }

    @Test
    fun `unauthenticated user cannot submit telemetry`() {
        val outcome = useCase(null, fix(), corridors, now = 100_000L)
        assertEquals(TelemetryRejection.NOT_AUTHENTICATED, (outcome as TelemetryOutcome.Rejected).reason)
    }

    @Test
    fun `poor accuracy fix is rejected`() {
        val outcome = useCase("user1", fix(accuracy = 200f), corridors, now = 100_000L)
        assertEquals(TelemetryRejection.POOR_ACCURACY, (outcome as TelemetryOutcome.Rejected).reason)
    }
}
