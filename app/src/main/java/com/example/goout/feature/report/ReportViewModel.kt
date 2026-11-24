package com.example.goout.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goout.core.network.ConnectivityObserver
import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.model.GeoPoint
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.CorridorRepository
import com.example.goout.domain.repository.LocationRepository
import com.example.goout.domain.usecase.ReportResult
import com.example.goout.domain.usecase.SubmitTrafficReportUseCase
import com.example.goout.domain.usecase.ValidateGeofenceUseCase
import com.example.goout.service.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    val isAuthenticated: StateFlow<Boolean> = authRepository.authState
        .map { it is com.example.goout.domain.repository.AuthState.Authenticated }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        refreshLocation()
    }

    fun refreshLocation() {
        _uiState.value = _uiState.value.copy(location = ReportLocationState.Checking)
        viewModelScope.launch {
            if (!locationRepository.hasLocationPermission()) {
                _uiState.value = _uiState.value.copy(location = ReportLocationState.PermissionNeeded)
                return@launch
            }
            val fix = locationRepository.getCurrentLocation()
            if (fix == null) {
                _uiState.value = _uiState.value.copy(location = ReportLocationState.Unavailable)
                return@launch
            }
            val corridors = corridorRepository.observeCorridors().first()
            val result = validateGeofence(GeoPoint(fix.latitude, fix.longitude), corridors, fix.accuracyMeters)
            _uiState.value = _uiState.value.copy(
                location = if (result.isValid && result.corridorId != null) {
                    val name = corridors.firstOrNull { it.corridorId == result.corridorId }?.corridorName ?: result.corridorId
                    ReportLocationState.Valid(result.corridorId, name, result.distanceMeters ?: 0.0)
                } else {
                    ReportLocationState.Invalid(result.distanceMeters)
                },
            )
        }
    }

    fun select(status: DensityStatus) {
        if (!_uiState.value.isSubmitting) {
            _uiState.value = _uiState.value.copy(selected = status)
        }
    }

    fun submit() {
        val selected = _uiState.value.selected ?: return
        if (_uiState.value.isSubmitting) return // prevent double-tap duplicates
        _uiState.value = _uiState.value.copy(isSubmitting = true)
        viewModelScope.launch {
            val result = submitReport(selected)
            if (result is ReportResult.SavedOffline) syncScheduler.scheduleSync()
            _uiState.value = _uiState.value.copy(isSubmitting = false, result = result)
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(result = null, selected = null)
    }
}
