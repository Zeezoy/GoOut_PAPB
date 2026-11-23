package com.example.goout.core.mock

import com.example.goout.BuildConfig
import com.example.goout.core.common.CorridorConfig
import com.example.goout.domain.repository.CorridorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * DEVELOPMENT-ONLY mock traffic generator.
 * Feeds simulated speeds (5–50 km/h) into the normal corridor aggregation
 * pipeline every [intervalMillis]. Only available in debug builds
 * (BuildConfig.ENABLE_MOCK_GENERATOR) and always off by default.
 * Simulated data goes through applyMeasurement like any measurement, but is
 * clearly bounded to local demo usage — it is never written as telemetry
 * events, so it can't pollute the telemetry collection.
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
        _isRunning.value = true
        job = appScope.launch {
            while (true) {
                CorridorConfig.corridors.forEach { seed ->
                    val speed = 5.0 + Random.nextDouble() * 45.0 // 5..50 km/h
                    corridorRepository.applyMeasurement(
                        seed.corridorId,
                        speed,
                        System.currentTimeMillis(),
                    )
                }
                delay(intervalMillis)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _isRunning.value = false
    }
}
