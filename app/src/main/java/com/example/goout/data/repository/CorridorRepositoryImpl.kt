package com.example.goout.data.repository

import com.example.goout.core.common.CorridorConfig
import com.example.goout.data.FirestoreSchema
import com.example.goout.data.decodePolyline
import com.example.goout.data.encodePolyline
import com.example.goout.data.local.dao.CorridorDao
import com.example.goout.data.local.entity.CorridorEntity
import com.example.goout.data.remote.firebase.FirebaseProvider
import com.example.goout.data.toDomain
import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.repository.CorridorRepository
import com.example.goout.domain.usecase.classifyTrafficDensity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local-first corridor repository:
 *  - UI observes Room ([observeCorridors]).
 *  - A Firestore snapshot listener mirrors cloud changes into Room.
 *  - [applyMeasurement] runs the deterministic aggregation policy and writes
 *    the result to Firestore (source of truth) and to the Room cache.
 *
 * Aggregation policy (documented engineering decision, MVP):
 *  - Measurements older than [CorridorConfig.AGGREGATION_WINDOW_MILLIS] are ignored.
 *  - Measurements older than the corridor's current lastUpdated are ignored
 *    (a stale reading can never overwrite a newer status).
 *  - Within the window the speed is smoothed with an exponential moving
 *    average (alpha = [CorridorConfig.EMA_ALPHA]); if the previous value is
 *    itself outside the window, the new measurement replaces it.
 *  - Status not updated for [CorridorConfig.STALE_AFTER_MILLIS] is surfaced
 *    as UNKNOWN by the entity->domain mapper.
 */
@Singleton
class CorridorRepositoryImpl @Inject constructor(
    private val corridorDao: CorridorDao,
    private val firebase: FirebaseProvider,
    private val appScope: CoroutineScope,
) : CorridorRepository {

    private var listener: ListenerRegistration? = null
    private val aggregationMutex = Mutex()

    override fun observeCorridors(): Flow<List<Corridor>> =
        corridorDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeCorridor(corridorId: String): Flow<Corridor?> =
        corridorDao.observeById(corridorId).map { it?.toDomain() }

    override suspend fun ensureSeeded() {
        if (corridorDao.count() == 0) {
            val seeds = CorridorConfig.corridors.map {
                CorridorEntity(
                    corridorId = it.corridorId,
                    corridorName = it.corridorName,
                    segmentLabel = it.segmentLabel,
                    polylineEncoded = it.polyline.encodePolyline(),
                    densityStatus = DensityStatus.UNKNOWN.name,
                    currentSpeed = null,
                    lastUpdated = 0L,
                    isSynced = false,
                )
            }
            corridorDao.upsertAll(seeds)
        }
        // Best-effort: make sure the corridor documents exist in Firestore so
        // other devices share the same configuration. Only geometry/metadata
        // is merged — never live traffic values.
        val firestore = firebase.firestore ?: return
        runCatching {
            CorridorConfig.corridors.forEach { seed ->
                val doc = firestore.collection(FirestoreSchema.CORRIDORS).document(seed.corridorId)
                val data = mapOf(
                    "corridorId" to seed.corridorId,
                    FirestoreSchema.CorridorFields.NAME to seed.corridorName,
                    FirestoreSchema.CorridorFields.SEGMENT_LABEL to seed.segmentLabel,
                    FirestoreSchema.CorridorFields.POLYLINE to seed.polyline.encodePolyline(),
                )
                doc.set(data, SetOptions.merge()).await()
            }
        }
    }

    override suspend fun startRemoteSync() {
        if (listener != null) return
        val firestore = firebase.firestore ?: return
        listener = firestore.collection(FirestoreSchema.CORRIDORS)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val docs = snapshot.documents
                appScope.launch {
                    val entities = docs.mapNotNull { it.toEntity() }
                    if (entities.isNotEmpty()) corridorDao.upsertAll(entities)
                }
            }
    }

    override suspend fun stopRemoteSync() {
        listener?.remove()
        listener = null
    }

    /**
     * FUNGSI 1 - Agregasi pengukuran kecepatan koridor.
     * [OWNER: Bastian] [FUNGSI 1]
     *
     * Kebijakan yang diharapkan:
     *  - kecepatan NaN atau negatif ditolak (error)
     *  - pengukuran lebih tua dari AGGREGATION_WINDOW_MILLIS diabaikan
     *  - pengukuran lebih tua dari lastUpdated koridor diabaikan
     *  - bila nilai sebelumnya masih segar: EMA (alpha = CorridorConfig.EMA_ALPHA),
     *    jika tidak: pakai nilai baru
     *  - status = classifyTrafficDensity(hasil agregasi)
     *  - tulis (merge) densityStatus, currentSpeed, lastUpdated ke Firestore, lalu ke Room
     *  - gunakan aggregationMutex agar pembaruan serentak tidak saling menimpa
     */
    override suspend fun applyMeasurement(corridorId: String, speedKmh: Double, timestamp: Long): Result<Unit> =
        runCatching {
            if (speedKmh.isNaN() || speedKmh.isInfinite() || speedKmh < 0) error("Invalid speed")
            aggregationMutex.withLock {
                val now = System.currentTimeMillis()
                if (now - timestamp > CorridorConfig.AGGREGATION_WINDOW_MILLIS) return@withLock

                val current = corridorDao.getById(corridorId) ?: error("Unknown corridor $corridorId")
                if (timestamp < current.lastUpdated) return@withLock

                val previousFresh = current.currentSpeed != null &&
                        now - current.lastUpdated <= CorridorConfig.AGGREGATION_WINDOW_MILLIS
                val aggregated = if (previousFresh) {
                    CorridorConfig.EMA_ALPHA * speedKmh +
                            (1 - CorridorConfig.EMA_ALPHA) * current.currentSpeed!!
                } else {
                    speedKmh
                }
                val status = classifyTrafficDensity(aggregated)
            }
        }

    private fun DocumentSnapshot.toEntity(): CorridorEntity? {
        val id = id.ifBlank { return null }
        val name = getString(FirestoreSchema.CorridorFields.NAME) ?: return null
        val polyline = getString(FirestoreSchema.CorridorFields.POLYLINE) ?: ""
        if (polyline.decodePolyline().isEmpty()) return null
        return CorridorEntity(
            corridorId = id,
            corridorName = name,
            segmentLabel = getString(FirestoreSchema.CorridorFields.SEGMENT_LABEL) ?: "",
            polylineEncoded = polyline,
            densityStatus = getString(FirestoreSchema.CorridorFields.DENSITY_STATUS)
                ?: DensityStatus.UNKNOWN.name,
            currentSpeed = getDouble(FirestoreSchema.CorridorFields.CURRENT_SPEED),
            lastUpdated = getLong(FirestoreSchema.CorridorFields.LAST_UPDATED) ?: 0L,
            isSynced = true,
        )
    }
}
