package com.example.goout.feature.report

import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.usecase.ReportResult

/** Geofence banner state on the report screen. */
sealed interface ReportLocationState {
    data object Checking : ReportLocationState
    data class Valid(val corridorId: String, val corridorName: String, val distanceMeters: Double) : ReportLocationState
    data class Invalid(val distanceMeters: Double?) : ReportLocationState
    data object Unavailable : ReportLocationState
    data object PermissionNeeded : ReportLocationState
}

data class ReportUiState(
    val selected: DensityStatus? = null,
    val isSubmitting: Boolean = false,
    val location: ReportLocationState = ReportLocationState.Checking,
    val result: ReportResult? = null,
    val isAuthenticated: Boolean = false,
)
