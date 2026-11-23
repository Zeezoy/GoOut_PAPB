package com.example.goout.core.common

import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.model.GeoPoint

/**
 * Central configuration for the three monitored corridors of Malang City.
 *
 * ⚠ GEOMETRY NOTE (engineering assumption, documented per project rules):
 * The coordinates below are APPROXIMATE centerline points for the three
 * well-known roads, placed from public map references. They are good enough
 * for development and demos, but before grading/production they SHOULD be
 * verified against actual road geometry (e.g. by tracing the roads in Google
 * Maps and replacing the lists below). All geometry lives ONLY here — update
 * this single file to correct it. Never treat these as measured traffic data.
 */
object CorridorConfig {

    const val CORRIDOR_VETERAN = "corridor_veteran"
    const val CORRIDOR_SOEKARNO_HATTA = "corridor_soehat"
    const val CORRIDOR_MT_HARYONO = "corridor_haryono"

    data class CorridorSeed(
        val corridorId: String,
        val corridorName: String,
        val segmentLabel: String,
        val polyline: List<GeoPoint>,
    )

    /** Jl. Veteran: Bundaran UB → Simpang Matos (west → east). */
    private val veteran = CorridorSeed(
        corridorId = CORRIDOR_VETERAN,
        corridorName = "Jl. Veteran",
        segmentLabel = "Bundaran UB → Simpang Matos",
        polyline = listOf(
            GeoPoint(-7.952549, 112.614432), // Bundaran UB (approx)
            GeoPoint(-7.956445, 112.613331),
            GeoPoint(-7.955962, 112.615460),
            GeoPoint(-7.958464, 112.619791), // near Matos (approx)
        ),
    )

    /** Jl. Soekarno-Hatta: Simpang Suhat → Jembatan Brantas (north → south). */
    private val soekarnoHatta = CorridorSeed(
        corridorId = CORRIDOR_SOEKARNO_HATTA,
        corridorName = "Jl. Soekarno Hatta",
        segmentLabel = "Simpang Suhat → Jembatan Brantas",
        polyline = listOf(
            GeoPoint(-7.936899, 112.626393), // Simpang Suhat (approx)
            GeoPoint(-7.939502, 112.624926),
            GeoPoint(-7.944557, 112.619401),
            GeoPoint(-7.949631, 112.615646), // Jembatan Soehat / Brantas (approx)
        ),
    )

    /** Jl. MT Haryono: Dinoyo → Simpang Betek (west → east). */
    private val mtHaryono = CorridorSeed(
        corridorId = CORRIDOR_MT_HARYONO,
        corridorName = "Jl. MT Haryono",
        segmentLabel = "Dinoyo → Simpang Betek",
        polyline = listOf(
            GeoPoint(-7.936675, 112.605828), // Dinoyo (approx)
            GeoPoint(-7.941447, 112.609124),
            GeoPoint(-7.946374, 112.612600),
            GeoPoint(-7.949659, 112.615088), // toward Betek (approx)
        ),
    )

    val corridors: List<CorridorSeed> = listOf(soekarnoHatta, veteran, mtHaryono)

    /**
     * Representative speed (km/h) attached to a one-tap report.
     * The source documentation does not prescribe exact values; these are
     * documented engineering assumptions chosen to sit safely inside each
     * classification band (LANCAR >= 35, 15 <= MERAYAP < 35, MACET < 15).
     */
    val representativeSpeeds: Map<DensityStatus, Double> = mapOf(
        DensityStatus.LANCAR to 40.0,
        DensityStatus.MERAYAP to 25.0,
        DensityStatus.MACET to 8.0,
    )

    /** Aggregation: measurements older than this are ignored. */
    const val AGGREGATION_WINDOW_MILLIS: Long = 10 * 60 * 1000L

    /** Corridor status becomes UNKNOWN when not updated within this window. */
    const val STALE_AFTER_MILLIS: Long = 30 * 60 * 1000L

    /** Minimum interval between passive telemetry submissions. */
    const val TELEMETRY_MIN_INTERVAL_MILLIS: Long = 10_000L

    /** Exponential moving average weight of the newest measurement. */
    const val EMA_ALPHA: Double = 0.4
}
