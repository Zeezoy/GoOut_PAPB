package com.example.goout.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached monitored corridor. The polyline is stored as a compact
 * "lat,lng;lat,lng;..." string (small, fixed geometry; no personal data).
 */
@Entity(tableName = "corridors")
data class CorridorEntity(
    @PrimaryKey val corridorId: String,
    val corridorName: String,
    val segmentLabel: String,
    val polylineEncoded: String,
    val densityStatus: String,
    val currentSpeed: Double?,
    val lastUpdated: Long,
    val isSynced: Boolean,
)

@Entity(
    tableName = "telemetry",
    indices = [Index("syncStatus"), Index("corridorId")],
)
data class TelemetryEntity(
    @PrimaryKey val telemetryId: String,
    val userId: String,
    val corridorId: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double,
    val timestamp: Long,
    val syncStatus: String,
    val source: String,
)

@Entity(
    tableName = "traffic_reports",
    indices = [Index("syncStatus"), Index("corridorId")],
)
data class TrafficReportEntity(
    @PrimaryKey val reportId: String,
    val userId: String,
    val corridorId: String,
    val reportedStatus: String,
    val representativeSpeed: Double,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val syncStatus: String,
)
