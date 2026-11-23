package com.example.goout.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "goout_prefs")

/**
 * Non-sensitive app preferences. Authentication/session state is handled
 * exclusively by Firebase Authentication — never stored here.
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val TELEMETRY_ENABLED = booleanPreferencesKey("passive_telemetry_enabled")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val MOCK_GENERATOR_ENABLED = booleanPreferencesKey("mock_generator_enabled")
    }

    val telemetryEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.TELEMETRY_ENABLED] ?: false }

    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    val mockGeneratorEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.MOCK_GENERATOR_ENABLED] ?: false }

    suspend fun setTelemetryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TELEMETRY_ENABLED] = enabled }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setMockGeneratorEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MOCK_GENERATOR_ENABLED] = enabled }
    }
}
