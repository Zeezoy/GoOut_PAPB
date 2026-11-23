package com.example.goout.core.mock

import com.example.goout.BuildConfig
import com.example.goout.domain.repository.CorridorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generator lalu lintas tiruan (hanya untuk pengembangan/demo).
 * [OWNER: Fevo] [FUNGSI 4]
 *
 * Yang diharapkan: tiap [intervalMillis] (10 detik) untuk setiap koridor di
 * CorridorConfig.corridors, buat kecepatan acak 5-50 km/jam dan kirim lewat
 * corridorRepository.applyMeasurement (bukan menulis ke koleksi telemetry).
 * Hanya aktif bila BuildConfig.ENABLE_MOCK_GENERATOR (build debug).
 */
@Singleton
class MockTrafficGenerator @Inject constructor(
    private val corridorRepository: CorridorRepository,
    private val appScope: CoroutineScope,
) {
    private var job: Job? = null
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    var intervalMillis: Long = 10_000L

    fun start() {
        if (!BuildConfig.ENABLE_MOCK_GENERATOR || job != null) return
        // TODO(Fevo): set _isRunning = true, luncurkan loop di appScope, simpan di job.
    }

    fun stop() {
        job?.cancel()
        job = null
        _isRunning.value = false
    }
}
