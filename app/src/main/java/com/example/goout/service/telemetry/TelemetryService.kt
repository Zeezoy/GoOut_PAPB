package com.example.goout.service.telemetry

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.goout.MainActivity
import com.example.goout.R
import com.example.goout.core.common.CorridorConfig
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.CorridorRepository
import com.example.goout.domain.repository.LocationRepository
import com.example.goout.domain.repository.TelemetryRepository
import com.example.goout.domain.usecase.ProcessTelemetryFixUseCase
import com.example.goout.domain.usecase.TelemetryOutcome
import com.example.goout.service.sync.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service for opt-in passive telemetry.
 * Shows a persistent notification while location is being collected,
 * stops itself when telemetry is disabled, the user logs out, or
 * permissions are revoked. Never started without explicit user opt-in.
 */
@AndroidEntryPoint
class TelemetryService : LifecycleService() {

    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var corridorRepository: CorridorRepository
    @Inject lateinit var telemetryRepository: TelemetryRepository
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var processFix: ProcessTelemetryFixUseCase
    @Inject lateinit var syncScheduler: SyncScheduler

    private var collectJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> start()
        }
        return START_STICKY
    }

    private fun start() {
        if (collectJob != null) return
        val user = authRepository.currentUser
        if (user == null || !locationRepository.hasLocationPermission()) {
            stopSelf()
            return
        }
        startInForeground()
        processFix.reset()
        collectJob = lifecycleScope.launch {
            locationRepository
                .locationUpdates(CorridorConfig.TELEMETRY_MIN_INTERVAL_MILLIS)
                .catch { stopSelf() }
                .collect { fix ->
                    // [OWNER: Keenan] [FUNGSI 3]
                    // TODO(Keenan): ambil koridor, proses fix dengan processFix, dan bila Accepted:
                    // simpan lokal (telemetryRepository.saveLocal), terapkan pengukuran ke koridor
                    // (corridorRepository.applyMeasurement), lalu jadwalkan sync.
                    val corridors = corridorRepository.observeCorridors().first()
                    val outcome = processFix(authRepository.currentUser?.uid, fix, corridors)
                    if (outcome is TelemetryOutcome.Accepted) {
                        telemetryRepository.saveLocal(outcome.event)
                        corridorRepository.applyMeasurement(
                            outcome.event.corridorId,
                            outcome.event.speedKmh,
                            outcome.event.timestamp,
                        )
                        syncScheduler.scheduleSync()
                    }
                }
        }
    }

    private fun startInForeground() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.telemetry_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.telemetry_notification_title))
            .setContentText(getString(R.string.telemetry_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        collectJob?.cancel()
        collectJob = null
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "telemetry_channel"
        private const val NOTIFICATION_ID = 42
        const val ACTION_STOP = "com.example.goout.action.STOP_TELEMETRY"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, TelemetryService::class.java))
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, TelemetryService::class.java).setAction(ACTION_STOP),
            )
        }
    }
}