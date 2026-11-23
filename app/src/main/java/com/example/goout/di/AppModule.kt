package com.example.goout.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.goout.data.local.GoOutDatabase
import com.example.goout.data.local.dao.CorridorDao
import com.example.goout.data.local.dao.TelemetryDao
import com.example.goout.data.local.dao.TrafficReportDao
import com.example.goout.data.repository.AuthRepositoryImpl
import com.example.goout.data.repository.CorridorRepositoryImpl
import com.example.goout.data.repository.LocationRepositoryImpl
import com.example.goout.data.repository.TelemetryRepositoryImpl
import com.example.goout.data.repository.TrafficReportRepositoryImpl
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.CorridorRepository
import com.example.goout.domain.repository.LocationRepository
import com.example.goout.domain.repository.TelemetryRepository
import com.example.goout.domain.repository.TrafficReportRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GoOutDatabase =
        Room.databaseBuilder(context, GoOutDatabase::class.java, "goout.db").build()

    @Provides fun provideCorridorDao(db: GoOutDatabase): CorridorDao = db.corridorDao()
    @Provides fun provideTelemetryDao(db: GoOutDatabase): TelemetryDao = db.telemetryDao()
    @Provides fun provideTrafficReportDao(db: GoOutDatabase): TrafficReportDao = db.trafficReportDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds abstract fun bindCorridorRepository(impl: CorridorRepositoryImpl): CorridorRepository
    @Binds abstract fun bindTelemetryRepository(impl: TelemetryRepositoryImpl): TelemetryRepository
    @Binds abstract fun bindTrafficReportRepository(impl: TrafficReportRepositoryImpl): TrafficReportRepository
    @Binds abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
}
