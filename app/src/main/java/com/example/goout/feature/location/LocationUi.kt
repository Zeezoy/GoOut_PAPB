package com.example.goout.feature.location

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.goout.feature.report.ReportLocationState

/**
 * FUNGSI 2 - Indikator "Lokasi Valid / Tidak Valid" dan alur izin lokasi.
 * [OWNER: Radit] [FUNGSI 2]
 *
 * Referensi prototipe: LocationBanner (lima state: Checking, Valid, Invalid,
 * Unavailable, PermissionNeeded) dan tombol "Izinkan Akses Lokasi".
 * State dan warna memakai GoOutColors dari design system.
 */
@Composable
fun LocationBanner(location: ReportLocationState, onRetry: () -> Unit) {
    // TODO(Radit): tampilkan banner sesuai [location]; klik banner memanggil onRetry.
    Text("TODO Radit: LocationBanner")
}

@Composable
fun LocationPermissionButton(onRequestPermission: () -> Unit, modifier: Modifier = Modifier) {
    // TODO(Radit): tombol "Izinkan Akses Lokasi" yang memanggil onRequestPermission.
}
