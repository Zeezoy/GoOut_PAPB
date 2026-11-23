package com.example.goout.domain.model

/**
 * Traffic density categories (core classification of the Go Out system).
 * UNKNOWN is used for missing or stale corridor data.
 */
enum class DensityStatus {
    LANCAR, MERAYAP, MACET, UNKNOWN;

    companion object {
        fun fromString(value: String?): DensityStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

/** Synchronization state of a locally stored record. */
enum class SyncStatus { PENDING, SYNCED, FAILED }

/** A geographic coordinate. */
data class GeoPoint(val latitude: Double, val longitude: Double) {
    val isValid: Boolean
        get() = latitude in -90.0..90.0 && longitude in -180.0..180.0 &&
            !(latitude == 0.0 && longitude == 0.0)
}

/** A monitored road corridor with its polyline geometry and latest traffic state. */
data class Corridor(
    val corridorId: String,
    val corridorName: String,
    /** Human readable segment description, e.g. "Bundaran UB → Simpang Matos". */
    val segmentLabel: String,
    /** Ordered polyline points of the monitored road segment. */
    val polyline: List<GeoPoint>,
    val densityStatus: DensityStatus,
    /** Latest aggregated speed in km/h; null when unknown. */
    val currentSpeed: Double?,
    /** Epoch millis of the last aggregation update; 0 when never updated. */
    val lastUpdated: Long,
)

/** Result of validating a device position against the monitored corridors. */
data class GeofenceResult(
    val isValid: Boolean,
    val corridorId: String?,
    val distanceMeters: Double?,
    val reason: GeofenceReason,
)

enum class GeofenceReason {
    WITHIN_CORRIDOR,
    OUTSIDE_CORRIDOR,
    INVALID_COORDINATES,
    NO_CORRIDOR_DATA,
    POOR_ACCURACY,
}

/** A single passive telemetry measurement. */
data class TelemetryEvent(
    val telemetryId: String,
    val userId: String,
    val corridorId: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double,
    val timestamp: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    /** "gps" for real measurements, "mock" for the development generator. */
    val source: String = "gps",
)

/** A one-tap active traffic report. */
data class TrafficReport(
    val reportId: String,
    val userId: String,
    val corridorId: String,
    val reportedStatus: DensityStatus,
    val representativeSpeed: Double,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
)

/** Minimal authenticated user profile. */
data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
)

/** A location fix from the fused location provider. */
data class LocationFix(
    val latitude: Double,
    val longitude: Double,
    /** Speed in m/s as reported by GPS; null when the fix has no speed. */
    val speedMetersPerSecond: Float?,
    /** Horizontal accuracy radius in meters; null when unknown. */
    val accuracyMeters: Float?,
    val timestamp: Long,
)
