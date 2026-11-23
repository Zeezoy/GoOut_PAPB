package com.example.goout

import com.example.goout.core.common.CorridorConfig
import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.model.GeoPoint
import com.example.goout.domain.model.LocationFix
import com.example.goout.domain.model.TrafficReport
import com.example.goout.domain.model.UserProfile
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.CorridorRepository
import com.example.goout.domain.repository.LocationRepository
import com.example.goout.domain.repository.TrafficReportRepository
import com.example.goout.domain.usecase.GeofenceValidator
import com.example.goout.domain.usecase.ReportResult
import com.example.goout.domain.usecase.SubmitTrafficReportUseCase
import com.example.goout.domain.usecase.ValidateGeofenceUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** CORE FUNCTION 4 — active 1-tap reporting tests. */
class SubmitTrafficReportUseCaseTest {

    private val authRepository: AuthRepository = mockk()
    private val locationRepository: LocationRepository = mockk()
    private val corridorRepository: CorridorRepository = mockk()
    private val reportRepository: TrafficReportRepository = mockk()

    private lateinit var useCase: SubmitTrafficReportUseCase

    private val corridor = Corridor(
        corridorId = "c1",
        corridorName = "Jl. Veteran",
        segmentLabel = "",
        polyline = listOf(GeoPoint(-0.001, 0.0), GeoPoint(0.001, 0.0)),
        densityStatus = DensityStatus.UNKNOWN,
        currentSpeed = null,
        lastUpdated = 0L,
    )

    private val insideFix = LocationFix(0.0001, 0.0, 5f, 10f, 1_000L)
    private val outsideFix = LocationFix(1.0, 1.0, 5f, 10f, 1_000L)

    @Before
    fun setup() {
        useCase = SubmitTrafficReportUseCase(
            authRepository,
            locationRepository,
            corridorRepository,
            reportRepository,
            ValidateGeofenceUseCase(GeofenceValidator()),
        )
        every { authRepository.currentUser } returns UserProfile("u1", "User", "u@x.com")
        every { corridorRepository.observeCorridors() } returns flowOf(listOf(corridor))
        coEvery { corridorRepository.applyMeasurement(any(), any(), any()) } returns Result.success(Unit)
        coEvery { reportRepository.saveLocal(any()) } just Runs
    }

    @Test
    fun `each status maps to its configured representative speed`() = runTest {
        coEvery { locationRepository.getCurrentLocation() } returns insideFix
        coEvery { reportRepository.submit(any()) } returns Result.success(Unit)

        for (status in listOf(DensityStatus.LANCAR, DensityStatus.MERAYAP, DensityStatus.MACET)) {
            val saved = slot<TrafficReport>()
            coEvery { reportRepository.saveLocal(capture(saved)) } just Runs
            val result = useCase(status)
            assertTrue(result is ReportResult.Delivered)
            assertEquals(status, saved.captured.reportedStatus)
            assertEquals(CorridorConfig.representativeSpeeds[status]!!, saved.captured.representativeSpeed, 0.0)
        }
    }

    @Test
    fun `valid location allows reporting and confirms cloud delivery`() = runTest {
        coEvery { locationRepository.getCurrentLocation() } returns insideFix
        coEvery { reportRepository.submit(any()) } returns Result.success(Unit)

        val result = useCase(DensityStatus.MACET)
        assertTrue(result is ReportResult.Delivered)
        coVerify { reportRepository.saveLocal(any()) }
        coVerify { reportRepository.submit(any()) }
        coVerify { corridorRepository.applyMeasurement("c1", any(), any()) }
    }

    @Test
    fun `invalid location rejects the report`() = runTest {
        coEvery { locationRepository.getCurrentLocation() } returns outsideFix

        val result = useCase(DensityStatus.LANCAR)
        assertTrue(result is ReportResult.InvalidLocation)
        coVerify(exactly = 0) { reportRepository.saveLocal(any()) }
    }

    @Test
    fun `missing location rejects the report`() = runTest {
        coEvery { locationRepository.getCurrentLocation() } returns null

        val result = useCase(DensityStatus.LANCAR)
        assertEquals(ReportResult.LocationUnavailable, result)
    }

    @Test
    fun `unauthenticated user cannot report`() = runTest {
        every { authRepository.currentUser } returns null

        val result = useCase(DensityStatus.LANCAR)
        assertEquals(ReportResult.NotAuthenticated, result)
        coVerify(exactly = 0) { reportRepository.saveLocal(any()) }
    }

    @Test
    fun `offline submission is saved locally and reported as pending, never as delivered`() = runTest {
        coEvery { locationRepository.getCurrentLocation() } returns insideFix
        coEvery { reportRepository.submit(any()) } returns Result.failure(Exception("offline"))

        val result = useCase(DensityStatus.MERAYAP)
        assertTrue(result is ReportResult.SavedOffline)
        coVerify { reportRepository.saveLocal(any()) }
    }
}
