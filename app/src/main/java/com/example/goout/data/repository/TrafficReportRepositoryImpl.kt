package com.example.goout.data.repository

import com.example.goout.data.FirestoreSchema
import com.example.goout.data.local.dao.TrafficReportDao
import com.example.goout.data.local.entity.TrafficReportEntity
import com.example.goout.data.remote.firebase.FirebaseProvider
import com.example.goout.data.toEntity
import com.example.goout.domain.model.SyncStatus
import com.example.goout.domain.model.TrafficReport
import com.example.goout.domain.repository.TrafficReportRepository
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrafficReportRepositoryImpl @Inject constructor(
    private val reportDao: TrafficReportDao,
    private val firebase: FirebaseProvider,
) : TrafficReportRepository {

    override suspend fun saveLocal(report: TrafficReport) {
        reportDao.insert(report.toEntity())
    }

    override suspend fun submit(report: TrafficReport): Result<Unit> = runCatching {
        val firestore = firebase.firestore ?: error("Firebase tidak tersedia")
        if (firebase.auth?.currentUser == null) error("Tidak terautentikasi")
        pushToFirestore(report.toEntity())
        reportDao.updateStatus(report.reportId, SyncStatus.SYNCED.name)
    }

    override suspend fun syncPending(): Result<Int> = runCatching {
        if (firebase.firestore == null) error("Firebase tidak tersedia")
        if (firebase.auth?.currentUser == null) error("Tidak terautentikasi")
        val pending = reportDao.pending()
        var synced = 0
        for (row in pending) {
            runCatching {
                pushToFirestore(row)
                reportDao.updateStatus(row.reportId, SyncStatus.SYNCED.name)
                synced++
            }.onFailure {
                reportDao.updateStatus(row.reportId, SyncStatus.FAILED.name)
                throw it
            }
        }
        synced
    }

    /** Stable document ID (= reportId) keeps retries idempotent. */
    private suspend fun pushToFirestore(row: TrafficReportEntity) {
        val firestore = firebase.firestore ?: error("Firebase tidak tersedia")
        firestore.collection(FirestoreSchema.REPORTS).document(row.reportId)
            .set(
                mapOf(
                    "reportId" to row.reportId,
                    "userId" to row.userId,
                    "corridorId" to row.corridorId,
                    "reportedStatus" to row.reportedStatus,
                    "representativeSpeed" to row.representativeSpeed,
                    "latitude" to row.latitude,
                    "longitude" to row.longitude,
                    "clientTimestamp" to row.timestamp,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
    }

    override fun observePendingCount(): Flow<Int> = reportDao.observePendingCount()
}
