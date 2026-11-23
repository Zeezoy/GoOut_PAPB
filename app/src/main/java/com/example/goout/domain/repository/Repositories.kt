package com.example.goout.domain.repository

import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.LocationFix
import com.example.goout.domain.model.TelemetryEvent
import com.example.goout.domain.model.TrafficReport
import com.example.goout.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

sealed interface AuthState {
    data object Loading : AuthState
    data object Guest : AuthState
    data class Authenticated(val user: UserProfile) : AuthState
}

interface AuthRepository {
    val authState: Flow<AuthState>
    val currentUser: UserProfile?
    suspend fun register(displayName: String, email: String, password: String): Result<UserProfile>
    suspend fun login(email: String, password: String): Result<UserProfile>
    suspend fun logout()
}

interface CorridorRepository {
    /** Local-first stream of the monitored corridors (Room cache, refreshed from Firestore). */
    fun observeCorridors(): Flow<List<Corridor>>
    fun observeCorridor(corridorId: String): Flow<Corridor?>

    /** Starts listening to Firestore corridor documents and mirrors them into Room. */
    suspend fun startRemoteSync()
    suspend fun stopRemoteSync()

    /** Seeds Room (and Firestore when authenticated/possible) with the configured corridor geometry. */
    suspend fun ensureSeeded()

    /**
     * Applies a new speed measurement to a corridor (deterministic aggregation,
     * see [com.example.goout.data.repository.CorridorRepositoryImpl]).
     */
    suspend fun applyMeasurement(corridorId: String, speedKmh: Double, timestamp: Long): Result<Unit>
}

interface TelemetryRepository {
    suspend fun saveLocal(event: TelemetryEvent)
    suspend fun syncPending(): Result<Int>
    fun observePendingCount(): Flow<Int>
}

interface TrafficReportRepository {
    suspend fun saveLocal(report: TrafficReport)
    /** Attempts to push one report to Firestore immediately; marks it SYNCED on success. */
    suspend fun submit(report: TrafficReport): Result<Unit>
    suspend fun syncPending(): Result<Int>
    fun observePendingCount(): Flow<Int>
}

interface LocationRepository {
    /** One-shot current location; null when unavailable. */
    suspend fun getCurrentLocation(): LocationFix?
    /** Continuous location updates (used by passive telemetry). */
    fun locationUpdates(intervalMillis: Long): Flow<LocationFix>
    fun isLocationEnabled(): Boolean
    fun hasLocationPermission(): Boolean
}
