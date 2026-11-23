package com.example.goout.domain.usecase

import com.example.goout.core.common.CorridorConfig
import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.model.GeofenceReason
import com.example.goout.domain.model.GeoPoint
import com.example.goout.domain.model.SyncStatus
import com.example.goout.domain.model.TrafficReport
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.CorridorRepository
import com.example.goout.domain.repository.LocationRepository
import com.example.goout.domain.repository.TrafficReportRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

/** Outcome of a one-tap report submission, mapped to UI feedback messages. */
sealed interface ReportResult {
    /** Report delivered to Firestore. */
    data class Delivered(val report: TrafficReport) : ReportResult

    /** Saved locally, awaiting synchronization (offline / cloud failure). */
    data class SavedOffline(val report: TrafficReport) : ReportResult

    data class InvalidLocation(val distanceMeters: Double?) : ReportResult
    data object LocationUnavailable : ReportResult
    data object NotAuthenticated : ReportResult
}

/**
 * CORE FUNCTION 4 — Active 1-tap reporting.
 * Auth check -> GPS fix -> geofence validation -> representative speed ->
 * local save -> cloud submit -> corridor aggregation.
 * Success is only reported after the cloud write is confirmed.
 */
class SubmitTrafficReportUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val corridorRepository: CorridorRepository,
    private val reportRepository: TrafficReportRepository,
    private val validateGeofence: ValidateGeofenceUseCase,
) {
    suspend operator fun invoke(selectedStatus: DensityStatus): ReportResult {
        val user = authRepository.currentUser ?: return ReportResult.NotAuthenticated

        val fix = locationRepository.getCurrentLocation() ?: return ReportResult.LocationUnavailable

        val corridors = corridorRepository.observeCorridors().first()
        val geofence = validateGeofence(
            GeoPoint(fix.latitude, fix.longitude),
            corridors,
            fix.accuracyMeters,
        )
        if (!geofence.isValid || geofence.corridorId == null) {
            return when (geofence.reason) {
                GeofenceReason.OUTSIDE_CORRIDOR -> ReportResult.InvalidLocation(geofence.distanceMeters)
                GeofenceReason.POOR_ACCURACY,
                GeofenceReason.INVALID_COORDINATES,
                -> ReportResult.LocationUnavailable
                else -> ReportResult.InvalidLocation(geofence.distanceMeters)
            }
        }

        val representativeSpeed = CorridorConfig.representativeSpeeds[selectedStatus]
            ?: return ReportResult.LocationUnavailable

        val report = TrafficReport(
            reportId = UUID.randomUUID().toString(),
            userId = user.uid,
            corridorId = geofence.corridorId,
            reportedStatus = selectedStatus,
            representativeSpeed = representativeSpeed,
            latitude = fix.latitude,
            longitude = fix.longitude,
            timestamp = System.currentTimeMillis(),
        )

        // Local-first: persist before attempting the network.
        reportRepository.saveLocal(report)

        val submitted = reportRepository.submit(report)
        return if (submitted.isSuccess) {
            corridorRepository.applyMeasurement(report.corridorId, representativeSpeed, report.timestamp)
            ReportResult.Delivered(report.copy(syncStatus = SyncStatus.SYNCED))
        } else {
            // Still update the local aggregation so the user's own map reflects it.
            corridorRepository.applyMeasurement(report.corridorId, representativeSpeed, report.timestamp)
            ReportResult.SavedOffline(report)
        }
    }
}
