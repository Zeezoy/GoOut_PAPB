package com.example.goout.domain.usecase

import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.model.TrafficReport
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.CorridorRepository
import com.example.goout.domain.repository.LocationRepository
import com.example.goout.domain.repository.TrafficReportRepository
import javax.inject.Inject

/** Hasil pengiriman laporan 1-tap, dipetakan ke pesan UI. */
sealed interface ReportResult {
    /** Laporan terkirim ke Firestore. */
    data class Delivered(val report: TrafficReport) : ReportResult

    /** Tersimpan lokal, menunggu sinkronisasi (offline / gagal cloud). */
    data class SavedOffline(val report: TrafficReport) : ReportResult

    data class InvalidLocation(val distanceMeters: Double?) : ReportResult
    data object LocationUnavailable : ReportResult
    data object NotAuthenticated : ReportResult
}

/**
 * FUNGSI 4 - Pelaporan aktif 1-tap.
 * [OWNER: Fevo] [FUNGSI 4]
 *
 * Alur: cek login -> ambil GPS -> validasi geofence -> kecepatan representatif
 * (CorridorConfig.representativeSpeeds) -> simpan lokal -> kirim ke cloud ->
 * agregasi koridor (applyMeasurement). "Delivered" hanya setelah cloud sukses.
 *
 * Test spesifikasi: SubmitTrafficReportUseCaseTest (6 test).
 */
class SubmitTrafficReportUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val corridorRepository: CorridorRepository,
    private val reportRepository: TrafficReportRepository,
    private val validateGeofence: ValidateGeofenceUseCase,
) {
    suspend operator fun invoke(selectedStatus: DensityStatus): ReportResult {
        // TODO(Fevo): implementasikan alur di atas.
        return ReportResult.LocationUnavailable
    }
}
