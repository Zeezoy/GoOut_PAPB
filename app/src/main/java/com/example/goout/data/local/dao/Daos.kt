package com.example.goout.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.goout.data.local.entity.CorridorEntity
import com.example.goout.data.local.entity.TelemetryEntity
import com.example.goout.data.local.entity.TrafficReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CorridorDao {
    @Query("SELECT * FROM corridors ORDER BY corridorName")
    fun observeAll(): Flow<List<CorridorEntity>>

    @Query("SELECT * FROM corridors WHERE corridorId = :id")
    fun observeById(id: String): Flow<CorridorEntity?>

    @Query("SELECT * FROM corridors WHERE corridorId = :id")
    suspend fun getById(id: String): CorridorEntity?

    @Query("SELECT COUNT(*) FROM corridors")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(entities: List<CorridorEntity>)

    @Upsert
    suspend fun upsert(entity: CorridorEntity)
}

@Dao
interface TelemetryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: TelemetryEntity)

    @Query("SELECT * FROM telemetry WHERE syncStatus = 'PENDING' ORDER BY timestamp LIMIT :limit")
    suspend fun pending(limit: Int = 50): List<TelemetryEntity>

    @Query("SELECT COUNT(*) FROM telemetry WHERE syncStatus = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("UPDATE telemetry SET syncStatus = :status WHERE telemetryId = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM telemetry WHERE syncStatus = 'SYNCED' AND timestamp < :olderThan")
    suspend fun pruneSynced(olderThan: Long)

    @Query("DELETE FROM telemetry WHERE userId = :userId AND syncStatus = 'PENDING'")
    suspend fun deletePendingForUser(userId: String)
}

@Dao
interface TrafficReportDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: TrafficReportEntity)

    @Query("SELECT * FROM traffic_reports WHERE syncStatus = 'PENDING' ORDER BY timestamp LIMIT :limit")
    suspend fun pending(limit: Int = 50): List<TrafficReportEntity>

    @Query("SELECT COUNT(*) FROM traffic_reports WHERE syncStatus = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("UPDATE traffic_reports SET syncStatus = :status WHERE reportId = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("SELECT * FROM traffic_reports WHERE reportId = :id")
    suspend fun getById(id: String): TrafficReportEntity?
}
