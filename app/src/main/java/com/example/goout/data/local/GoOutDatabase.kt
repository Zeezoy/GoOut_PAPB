package com.example.goout.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.goout.data.local.dao.CorridorDao
import com.example.goout.data.local.dao.TelemetryDao
import com.example.goout.data.local.dao.TrafficReportDao
import com.example.goout.data.local.entity.CorridorEntity
import com.example.goout.data.local.entity.TelemetryEntity
import com.example.goout.data.local.entity.TrafficReportEntity

@Database(
    entities = [CorridorEntity::class, TelemetryEntity::class, TrafficReportEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class GoOutDatabase : RoomDatabase() {
    abstract fun corridorDao(): CorridorDao
    abstract fun telemetryDao(): TelemetryDao
    abstract fun trafficReportDao(): TrafficReportDao
}
