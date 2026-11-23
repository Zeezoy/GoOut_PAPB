package com.example.goout.fakes

import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.model.GeoPoint
import com.example.goout.domain.model.LocationFix
import com.example.goout.domain.model.TrafficReport
import com.example.goout.domain.model.UserProfile
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.AuthState
import com.example.goout.domain.repository.CorridorRepository
import com.example.goout.domain.repository.LocationRepository
import com.example.goout.domain.repository.TrafficReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeAuthRepository(initial: AuthState = AuthState.Guest) : AuthRepository {
    val state = MutableStateFlow(initial)
    override val authState: Flow<AuthState> = state
    override val currentUser: UserProfile?
        get() = (state.value as? AuthState.Authenticated)?.user

    var loginResult: Result<UserProfile> = Result.failure(IllegalStateException("not configured"))
    var lastLogin: Pair<String, String>? = null

    override suspend fun register(displayName: String, email: String, password: String): Result<UserProfile> =
        loginResult

    override suspend fun login(email: String, password: String): Result<UserProfile> {
        lastLogin = email to password
        return loginResult
    }

    override suspend fun logout() { state.value = AuthState.Guest }
}

class FakeLocationRepository(
    var permission: Boolean = true,
    var fix: LocationFix? = null,
) : LocationRepository {
    override suspend fun getCurrentLocation(): LocationFix? = fix
    override fun locationUpdates(intervalMillis: Long): Flow<LocationFix> = flowOf()
    override fun isLocationEnabled(): Boolean = fix != null
    override fun hasLocationPermission(): Boolean = permission
}

class FakeCorridorRepository(private val corridors: List<Corridor>) : CorridorRepository {
    override fun observeCorridors(): Flow<List<Corridor>> = flowOf(corridors)
    override fun observeCorridor(corridorId: String): Flow<Corridor?> =
        flowOf(corridors.firstOrNull { it.corridorId == corridorId })
    override suspend fun startRemoteSync() {}
    override suspend fun stopRemoteSync() {}
    override suspend fun ensureSeeded() {}
    override suspend fun applyMeasurement(corridorId: String, speedKmh: Double, timestamp: Long): Result<Unit> =
        Result.success(Unit)
}

class FakeTrafficReportRepository(var submitResult: Result<Unit> = Result.success(Unit)) : TrafficReportRepository {
    val saved = mutableListOf<TrafficReport>()
    override suspend fun saveLocal(report: TrafficReport) { saved += report }
    override suspend fun submit(report: TrafficReport): Result<Unit> = submitResult
    override suspend fun syncPending(): Result<Int> = Result.success(0)
    override fun observePendingCount(): Flow<Int> = flowOf(saved.size)
}

object TestData {
    val user = UserProfile(uid = "test-uid", displayName = "Penguji", email = "penguji@example.com")

    /** Matches the Jl. Veteran seed geometry in CorridorConfig. */
    val veteran = Corridor(
        corridorId = "corridor_veteran",
        corridorName = "Jl. Veteran",
        segmentLabel = "Bundaran UB → Simpang Matos",
        polyline = listOf(
            GeoPoint(-7.952778, 112.611944),
            GeoPoint(-7.955278, 112.613611),
            GeoPoint(-7.957500, 112.616111),
            GeoPoint(-7.959167, 112.618889),
        ),
        densityStatus = DensityStatus.LANCAR,
        currentSpeed = 40.0,
        lastUpdated = 0L,
    )

    /** A fix directly on the first Veteran polyline point (inside the 100 m geofence). */
    fun fixOnVeteran() = LocationFix(
        latitude = -7.952778,
        longitude = 112.611944,
        speedMetersPerSecond = 5f,
        accuracyMeters = 10f,
        timestamp = System.currentTimeMillis(),
    )

    /** A fix in Surabaya, ~90 km from every monitored corridor. */
    fun fixFarAway() = LocationFix(
        latitude = -7.257472,
        longitude = 112.752088,
        speedMetersPerSecond = 5f,
        accuracyMeters = 10f,
        timestamp = System.currentTimeMillis(),
    )
}
