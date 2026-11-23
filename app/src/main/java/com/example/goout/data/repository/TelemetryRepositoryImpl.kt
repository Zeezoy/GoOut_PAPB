package com.example.goout.data.repository

import com.example.goout.data.local.dao.TelemetryDao
import com.example.goout.data.remote.firebase.FirebaseProvider
import com.example.goout.domain.model.TelemetryEvent
import com.example.goout.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FUNGSI 3 - Repository telemetri (Room + Firestore).
 * [OWNER: Keenan] [FUNGSI 3]
 *
 * Yang diharapkan: saveLocal menyimpan event PENDING ke Room. syncPending
 * mengirim PENDING ke koleksi `telemetry` (ID dokumen = telemetryId, retry
 * idempoten, `createdAt` server timestamp), menandai SYNCED atau FAILED, lalu
 * membersihkan data SYNCED yang lama.
 */
@Singleton
class TelemetryRepositoryImpl @Inject constructor(
    private val telemetryDao: TelemetryDao,
    private val firebase: FirebaseProvider,
) : TelemetryRepository {

    override suspend fun saveLocal(event: TelemetryEvent) {
        // TODO(Keenan): simpan ke Room.
    }

    override suspend fun syncPending(): Result<Int> {
        // TODO(Keenan): kirim semua telemetri PENDING ke Firestore.
        return Result.success(0)
    }

    override fun observePendingCount(): Flow<Int> = telemetryDao.observePendingCount()
}
