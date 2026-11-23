package com.example.goout.data

import com.example.goout.data.local.entity.CorridorEntity
import com.example.goout.data.local.entity.TelemetryEntity
import com.example.goout.data.local.entity.TrafficReportEntity
import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.model.GeoPoint
import com.example.goout.domain.model.SyncStatus
import com.example.goout.domain.model.TelemetryEvent
import com.example.goout.domain.model.TrafficReport

/** "lat,lng;lat,lng" <-> List<GeoPoint> */
fun List<GeoPoint>.encodePolyline(): String =
    joinToString(";") { "${it.latitude},${it.longitude}" }

fun String.decodePolyline(): List<GeoPoint> =
    split(";").mapNotNull { pair ->
        val parts = pair.split(",")
        val lat = parts.getOrNull(0)?.toDoubleOrNull()
        val lng = parts.getOrNull(1)?.toDoubleOrNull()
        if (lat != null && lng != null) GeoPoint(lat, lng) else null
    }

fun CorridorEntity.toDomain(now: Long = System.currentTimeMillis()): Corridor {
    val stale = lastUpdated <= 0L ||
        now - lastUpdated > com.example.goout.core.common.CorridorConfig.STALE_AFTER_MILLIS
    return Corridor(
        corridorId = corridorId,
        corridorName = corridorName,
        segmentLabel = segmentLabel,
        polyline = polylineEncoded.decodePolyline(),
        densityStatus = if (stale) DensityStatus.UNKNOWN else DensityStatus.fromString(densityStatus),
        currentSpeed = if (stale) null else currentSpeed,
        lastUpdated = lastUpdated,
    )
}

fun TelemetryEvent.toEntity() = TelemetryEntity(
    telemetryId, userId, corridorId, latitude, longitude, speedKmh, timestamp, syncStatus.name, source,
)

fun TelemetryEntity.toDomain() = TelemetryEvent(
    telemetryId, userId, corridorId, latitude, longitude, speedKmh, timestamp,
    SyncStatus.valueOf(syncStatus), source,
)

fun TrafficReport.toEntity() = TrafficReportEntity(
    reportId, userId, corridorId, reportedStatus.name, representativeSpeed,
    latitude, longitude, timestamp, syncStatus.name,
)

fun TrafficReportEntity.toDomain() = TrafficReport(
    reportId, userId, corridorId, DensityStatus.fromString(reportedStatus),
    representativeSpeed, latitude, longitude, timestamp, SyncStatus.valueOf(syncStatus),
)

/** Firestore collection and field names (single source of truth). */
object FirestoreSchema {
    const val USERS = "users"
    const val CORRIDORS = "corridors"
    const val TELEMETRY = "telemetry"
    const val REPORTS = "reports"

    object CorridorFields {
        const val NAME = "corridorName"
        const val SEGMENT_LABEL = "segmentLabel"
        const val POLYLINE = "polyline"
        const val DENSITY_STATUS = "densityStatus"
        const val CURRENT_SPEED = "currentSpeed"
        const val LAST_UPDATED = "lastUpdated"
    }
}
