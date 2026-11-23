package com.example.goout.service.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.goout.domain.usecase.SyncPendingReportsUseCase
import com.example.goout.domain.usecase.SyncPendingTelemetryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deferred synchronization of PENDING telemetry and reports.
 * Runs only with network connectivity; retries with exponential backoff.
 * Writes use stable document IDs, so retried syncs are idempotent.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncTelemetry: SyncPendingTelemetryUseCase,
    private val syncReports: SyncPendingReportsUseCase,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val telemetry = syncTelemetry()
        val reports = syncReports()
        return if (telemetry.isSuccess && reports.isSuccess) {
            Result.success()
        } else if (runAttemptCount < MAX_RETRIES) {
            Result.retry()
        } else {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "goout_sync"
        private const val MAX_RETRIES = 5
    }
}

@Singleton
class SyncScheduler @Inject constructor(
    private val workManager: WorkManager,
) {
    fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(SyncWorker.WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }
}
