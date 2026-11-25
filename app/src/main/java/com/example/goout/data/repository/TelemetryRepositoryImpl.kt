package com.example.goout.data.repository

import com.example.goout.data.FirestoreSchema
import com.example.goout.data.local.dao.TelemetryDao
import com.example.goout.data.remote.firebase.FirebaseProvider
import com.example.goout.data.toEntity
import com.example.goout.domain.model.SyncStatus
import com.example.goout.domain.model.TelemetryEvent
import com.example.goout.domain.repository.TelemetryRepository
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
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
        telemetryDao.insert(event.toEntity())
    }

    override suspend fun syncPending(): Result<Int> = runCatching {
        // TODO(Keenan): kirim semua telemetri PENDING ke Firestore.
        val firestore = firebase.firestore ?: error("Firebase tidak tersedia")
        if (firebase.auth?.currentUser == null) error("Tidak terautentikasi")
        val pending = telemetryDao.pending()
        var synced = 0
        for (row in pending) {
            runCatching {
                firestore.collection(FirestoreSchema.TELEMETRY).document(row.telemetryId)
                    .set(
                        mapOf(
                            "telemetryId" to row.telemetryId,
                            "userId" to row.userId,
                            "corridorId" to row.corridorId,
                            "latitude" to row.latitude,
                            "longitude" to row.longitude,
                            "speedKmh" to row.speedKmh,
                            "clientTimestamp" to row.timestamp,
                            "source" to row.source,
                            "createdAt" to FieldValue.serverTimestamp(),
                        ),
                    ).await()
                telemetryDao.updateStatus(row.telemetryId, SyncStatus.SYNCED.name)
                synced++
            }.onFailure {
                telemetryDao.updateStatus(row.telemetryId, SyncStatus.FAILED.name)
                throw it
            }
        }
        telemetryDao.pruneSynced(System.currentTimeMillis() - 24 * 60 * 60 * 1000L)
        synced
    }
    override fun observePendingCount(): Flow<Int> = telemetryDao.observePendingCount()
}
