package com.example.goout.data.repository

import com.example.goout.data.local.dao.TrafficReportDao
import com.example.goout.data.remote.firebase.FirebaseProvider
import com.example.goout.domain.model.TrafficReport
import com.example.goout.domain.repository.TrafficReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FUNGSI 4 - Repository laporan (Room + Firestore).
 * [OWNER: Fevo] [FUNGSI 4]
 *
 * Yang diharapkan: simpan lokal dulu (status PENDING), submit ke koleksi `reports`
 * dengan ID dokumen = reportId (retry idempoten) dan `createdAt` server timestamp,
 * lalu tandai SYNCED. syncPending mengirim antrean PENDING.
 */
@Singleton
class TrafficReportRepositoryImpl @Inject constructor(
    private val reportDao: TrafficReportDao,
    private val firebase: FirebaseProvider,
) : TrafficReportRepository {

    override suspend fun saveLocal(report: TrafficReport) {
        // TODO(Fevo): simpan ke Room.
    }

    override suspend fun submit(report: TrafficReport): Result<Unit> {
        // TODO(Fevo): kirim ke Firestore, tandai SYNCED bila sukses.
        return Result.failure(Exception("TODO(Fevo): submit belum diimplementasikan"))
    }

    override suspend fun syncPending(): Result<Int> {
        // TODO(Fevo): kirim semua laporan PENDING.
        return Result.success(0)
    }

    override fun observePendingCount(): Flow<Int> = reportDao.observePendingCount()
}
