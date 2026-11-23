package com.example.goout.feature.report

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Layar "Lapor" - pelaporan kondisi lalu lintas 1-tap.
 * [OWNER: Fevo] [FUNGSI 4]
 *
 * Yang diharapkan:
 *  - Jika state.result tidak null, tampilkan ResultPanel sesuai ReportResult
 *    (Delivered, SavedOffline, InvalidLocation, LocationUnavailable, NotAuthenticated)
 *  - Jika tidak: judul, LocationBanner (milik Radit, feature/location), ajakan login
 *    bila belum login, 3 ReportOption (LANCAR, MERAYAP, MACET), tombol "Kirim Laporan Kondisi"
 *    dan "Batal".
 *  Komponen ReportOption dan ResultPanel tersedia di ReportComponents.kt.
 */
@Composable
fun ReportScreen(
    onBackToMap: () -> Unit,
    onLogin: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    // TODO(Fevo)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("TODO Fevo: layar Report")
    }
}
