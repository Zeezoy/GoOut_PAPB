package com.example.goout.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goout.BuildConfig
import com.example.goout.core.mock.MockTrafficGenerator
import com.example.goout.data.local.datastore.UserPreferencesDataSource
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.AuthState
import com.example.goout.domain.repository.LocationRepository
import com.example.goout.domain.repository.TelemetryRepository
import com.example.goout.domain.repository.TrafficReportRepository
import com.example.goout.service.telemetry.TelemetryService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val authState: AuthState = AuthState.Loading,
    val telemetryEnabled: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val pendingTelemetry: Int = 0,
    val pendingReports: Int = 0,
    val mockAvailable: Boolean = BuildConfig.ENABLE_MOCK_GENERATOR,
    val mockRunning: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val preferences: UserPreferencesDataSource,
    private val locationRepository: LocationRepository,
    private val mockGenerator: MockTrafficGenerator,
    telemetryRepository: TelemetryRepository,
    reportRepository: TrafficReportRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.authState,
        preferences.telemetryEnabled,
        telemetryRepository.observePendingCount(),
        reportRepository.observePendingCount(),
        mockGenerator.isRunning,
    ) { auth, telemetry, pendingTel, pendingRep, mockRunning ->
        ProfileUiState(
            authState = auth,
            telemetryEnabled = telemetry,
            hasLocationPermission = locationRepository.hasLocationPermission(),
            pendingTelemetry = pendingTel,
            pendingReports = pendingRep,
            mockRunning = mockRunning,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    /** Opt-in toggle: starts/stops the visible foreground telemetry service. */
    fun setTelemetryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val isAuthenticated = authRepository.currentUser != null
            val allowed = enabled && isAuthenticated && locationRepository.hasLocationPermission()
            preferences.setTelemetryEnabled(allowed)
            if (allowed) TelemetryService.start(context) else TelemetryService.stop(context)
        }
    }

    fun toggleMockGenerator() {
        viewModelScope.launch {
            if (mockGenerator.isRunning.value) {
                mockGenerator.stop()
                preferences.setMockGeneratorEnabled(false)
            } else {
                mockGenerator.start()
                preferences.setMockGeneratorEnabled(true)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            TelemetryService.stop(context)
            mockGenerator.stop()
            authRepository.logout()
        }
    }
}
