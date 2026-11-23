package com.example.goout

import com.example.goout.core.common.CorridorConfig
import com.example.goout.core.utils.GeoUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Penjaga sederhana: lat/lng tidak tertukar dan titik tidak meloncat. */
class CorridorConfigTest {

    @Test
    fun tiga_koridor_terdaftar() {
        assertEquals(3, CorridorConfig.corridors.size)
    }

    @Test
    fun titik_cukup_dan_berada_di_area_malang() {
        CorridorConfig.corridors.forEach { c ->
            assertTrue("${c.corridorId}: minimal 4 titik", c.polyline.size >= 4)
            c.polyline.forEach { p ->
                assertTrue("${c.corridorId}: lat di luar Malang ${p.latitude}", p.latitude in -8.05..-7.85)
                assertTrue("${c.corridorId}: lng di luar Malang ${p.longitude}", p.longitude in 112.55..112.72)
            }
        }
    }

    @Test
    fun jarak_antar_titik_wajar() {
        CorridorConfig.corridors.forEach { c ->
            c.polyline.zipWithNext().forEach { (a, b) ->
                val d = GeoUtils.haversineMeters(a, b)
                assertTrue("${c.corridorId}: terlalu dekat (${d.toInt()} m)", d >= 15)
                assertTrue("${c.corridorId}: loncatan terlalu jauh (${d.toInt()} m)", d <= 600)
            }
        }
    }
}