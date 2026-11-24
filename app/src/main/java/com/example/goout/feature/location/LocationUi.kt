package com.example.goout.feature.location

import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.goout.feature.report.ReportLocationState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goout.core.designsystem.GoOutColors

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
    val (latar, teksWarna, pesan) = when (location) {
        ReportLocationState.Checking -> Triple(
            GoOutColors.SurfaceCard,
            GoOutColors.TextSecondary,
            "Memeriksa lokasi Anda…"
        )
        is ReportLocationState.Valid -> Triple(
            GoOutColors.LancarContainer,
            GoOutColors.OnLancarContainer,
            "Lokasi Terverifikasi di Koridor ${location.corridorName} (±${location.distanceMeters.toInt()} m)"
        )
        is ReportLocationState.Invalid -> Triple(
            GoOutColors.MacetContainer,
            GoOutColors.OnMacetContainer,
            "Lokasi Anda berada di luar area koridor pemantauan."
        )
        ReportLocationState.Unavailable -> Triple(
            GoOutColors.MacetContainer,
            GoOutColors.OnMacetContainer,
            "Tidak dapat memperoleh lokasi. Pastikan GPS aktif."
        )
        ReportLocationState.PermissionNeeded -> Triple(
            GoOutColors.MerayapContainer,
            GoOutColors.OnMerayapContainer,
            "Izin lokasi diperlukan untuk memvalidasi posisi Anda."
        )
    }
    Row(
        Modifier.fillMaxWidth().background(latar, RoundedCornerShape(12.dp))
            .clickable(onClick = onRetry).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (location is ReportLocationState.Checking) CircularProgressIndicator(Modifier.size(16.dp), color = teksWarna, strokeWidth = 2.dp)
        else Icon(Icons.Filled.LocationOn, contentDescription = null, tint = teksWarna, modifier = Modifier.size(18.dp))
        Text("  $pesan", style = MaterialTheme.typography.bodySmall, color = teksWarna, modifier = Modifier.weight(1f))
    }
}

@Composable
fun LocationPermissionButton(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onRequestPermission,
        modifier = modifier
            .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(999.dp)),
    ) {
        Text("Izinkan Akses Lokasi", color = GoOutColors.Primary, fontSize = 12.sp)
    }
}
