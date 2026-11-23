package com.example.goout.domain.usecase

import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.LocationFix
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.AuthState
import com.example.goout.domain.repository.CorridorRepository
import com.example.goout.domain.repository.LocationRepository
import com.example.goout.domain.repository.TelemetryRepository
import com.example.goout.domain.repository.TrafficReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCorridorsUseCase @Inject constructor(private val repo: CorridorRepository) {
    operator fun invoke(): Flow<List<Corridor>> = repo.observeCorridors()
}

class GetCorridorDetailUseCase @Inject constructor(private val repo: CorridorRepository) {
    operator fun invoke(corridorId: String): Flow<Corridor?> = repo.observeCorridor(corridorId)
}

class GetCurrentLocationUseCase @Inject constructor(private val repo: LocationRepository) {
    suspend operator fun invoke(): LocationFix? = repo.getCurrentLocation()
}

class ObserveAuthenticationStateUseCase @Inject constructor(private val repo: AuthRepository) {
    operator fun invoke(): Flow<AuthState> = repo.authState
}

class SyncPendingTelemetryUseCase @Inject constructor(private val repo: TelemetryRepository) {
    suspend operator fun invoke(): Result<Int> = repo.syncPending()
}

class SyncPendingReportsUseCase @Inject constructor(private val repo: TrafficReportRepository) {
    suspend operator fun invoke(): Result<Int> = repo.syncPending()
}
