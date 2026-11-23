package com.example.goout.feature.report

import androidx.lifecycle.ViewModel
import com.example.goout.core.network.ConnectivityObserver
import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.CorridorRepository
import com.example.goout.domain.repository.LocationRepository
import com.example.goout.domain.usecase.SubmitTrafficReportUseCase
import com.example.goout.domain.usecase.ValidateGeofenceUseCase
import com.example.goout.service.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * FUNGSI 4 - State dan event layar Report.
 * [OWNER: Fevo] [FUNGSI 4]
 *
 * Tipe state ada di ReportState.kt (ReportUiState, ReportLocationState).
 * Yang diharapkan:
 *  - refreshLocation(): cek izin, ambil lokasi, validasi geofence, isi ReportLocationState
 *  - select(status): pilih status (diabaikan saat isSubmitting)
 *  - submit(): kirim lewat use case, cegah ketukan ganda, jadwalkan sync bila SavedOffline
 *  - clearResult(): reset hasil dan pilihan
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val submitReport: SubmitTrafficReportUseCase,
    private val locationRepository: LocationRepository,
    private val corridorRepository: CorridorRepository,
    private val validateGeofence: ValidateGeofenceUseCase,
    private val syncScheduler: SyncScheduler,
    authRepository: AuthRepository,
    val connectivity: ConnectivityObserver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    // TODO(Fevo): turunkan dari authRepository.authState (Authenticated -> true).
    val isAuthenticated: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

    fun refreshLocation() {
        // TODO(Fevo)
    }

    fun select(status: DensityStatus) {
        // TODO(Fevo)
    }

    fun submit() {
        // TODO(Fevo)
    }

    fun clearResult() {
        // TODO(Fevo)
    }
}
