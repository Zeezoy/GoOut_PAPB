package com.example.goout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.example.goout.core.network.ConnectivityObserver
import com.example.goout.domain.repository.AuthState
import com.example.goout.domain.usecase.GeofenceValidator
import com.example.goout.domain.usecase.SubmitTrafficReportUseCase
import com.example.goout.domain.usecase.ValidateGeofenceUseCase
import com.example.goout.fakes.FakeAuthRepository
import com.example.goout.fakes.FakeCorridorRepository
import com.example.goout.fakes.FakeLocationRepository
import com.example.goout.fakes.FakeTrafficReportRepository
import com.example.goout.fakes.TestData
import com.example.goout.feature.report.ReportScreen
import com.example.goout.feature.report.ReportViewModel
import com.example.goout.service.sync.SyncScheduler
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for the 1-tap report flow: success, out-of-geofence failure, permission-denied. */
@RunWith(AndroidJUnit4::class)
class ReportUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun launchReport(
        location: FakeLocationRepository,
        reports: FakeTrafficReportRepository = FakeTrafficReportRepository(),
        authState: AuthState = AuthState.Authenticated(TestData.user),
    ) {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val auth = FakeAuthRepository(authState)
        val corridors = FakeCorridorRepository(listOf(TestData.veteran))
        val validate = ValidateGeofenceUseCase(GeofenceValidator())
        val viewModel = ReportViewModel(
            submitReport = SubmitTrafficReportUseCase(auth, location, corridors, reports, validate),
            locationRepository = location,
            corridorRepository = corridors,
            validateGeofence = validate,
            syncScheduler = SyncScheduler(WorkManager.getInstance(context)),
            authRepository = auth,
            connectivity = ConnectivityObserver(context),
        )
        composeRule.setContent {
            ReportScreen(onBackToMap = {}, onLogin = {}, viewModel = viewModel)
        }
    }

    @Test
    fun report_oneTapInsideGeofence_showsSuccessScreen() {
        launchReport(location = FakeLocationRepository(fix = TestData.fixOnVeteran()))

        // Geofence banner confirms the verified corridor first.
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Jl. Veteran", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("LANCAR").performClick()
        composeRule.onNodeWithText("Kirim Laporan Kondisi").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Laporan Berhasil Terkirim"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Laporan Berhasil Terkirim").assertIsDisplayed()
    }

    @Test
    fun report_outsideGeofence_showsInvalidLocationScreen() {
        launchReport(location = FakeLocationRepository(fix = TestData.fixFarAway()))

        composeRule.onNodeWithText("MACET").performClick()
        composeRule.onNodeWithText("Kirim Laporan Kondisi").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Lokasi Tidak Valid"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Lokasi Tidak Valid").assertIsDisplayed()
    }

    @Test
    fun report_withoutLocationPermission_showsPermissionBanner() {
        launchReport(location = FakeLocationRepository(permission = false, fix = null))

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasText("Izin lokasi diperlukan", substring = true),
            ).fetchSemanticsNodes().isNotEmpty()
        }
        // The banner renders the text with a leading-space prefix, so match by substring.
        composeRule
            .onNode(androidx.compose.ui.test.hasText("Izin lokasi diperlukan", substring = true))
            .assertIsDisplayed()
    }

    @Test
    fun report_gpsUnavailable_showsUnavailableBanner() {
        launchReport(location = FakeLocationRepository(permission = true, fix = null))

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasText("Tidak dapat memperoleh lokasi", substring = true),
            ).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun report_asGuest_showsLoginPrompt() {
        launchReport(
            location = FakeLocationRepository(fix = TestData.fixOnVeteran()),
            authState = AuthState.Guest,
        )
        composeRule.onNodeWithText("Silakan login untuk mengirim laporan.").assertIsDisplayed()
    }
}
