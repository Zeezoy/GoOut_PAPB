package com.example.goout.feature.corridor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goout.core.designsystem.CorridorCard
import com.example.goout.core.designsystem.GoOutColors
import com.example.goout.core.designsystem.StatusBadge
import com.example.goout.core.designsystem.relativeTime
import com.example.goout.core.designsystem.statusColor
import com.example.goout.domain.model.DensityStatus
import com.example.goout.feature.home.HomeViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.goout.core.designsystem.MacetWarning
import com.google.android.gms.maps.model.Polygon

/**
 * Layar "Koridor" dan "Detail Koridor".
 * [OWNER: Bastian] [FUNGSI 1]
 *
 * Referensi tampilan: daftar tiga koridor (CorridorCard dari design system) dan
 * detail koridor (nama, segmen, status berwarna, kecepatan rata-rata dengan angka besar,
 * waktu pembaruan terakhir, tombol "Laporkan Kondisi" dan kembali).
 * State detail ada di CorridorDetailViewModel.
 */
@Composable
fun CorridorListScreen(
    onCorridorClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(GoOutColors.Background)
            .padding(16.dp)
    ) {
        Text(
            "Koridor Terpantau",
            style = MaterialTheme.typography.titleLarge,
            color = GoOutColors.TextPrimary
        )
        Text(
            "3 koridor utama Kota Malang",
            style = MaterialTheme.typography.bodySmall,
            color = GoOutColors.TextSecondary
        )
        Spacer(Modifier.height(16.dp))
        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoOutColors.Primary)
            }
        } else if (state.corridors.isEmpty()) {
            Text(
                "Belum adda data koridor. Perikasalah koneksi internet Anda.",
                style = MaterialTheme.typography.bodySmall,
                color = GoOutColors.TextSecondary,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.corridors, key = { it.corridorId }) { corridor ->
                    CorridorCard(
                        corridor = corridor,
                        onClick = { onCorridorClick(corridor.corridorId) })
                }
            }
        }
    }
}
@Composable
fun CorridorDetailScreen(
    onBack: () -> Unit,
    onReport: () -> Unit,
    viewModel: CorridorDetailViewModel = hiltViewModel(),
) {
    val corridor by viewModel.corridor.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(GoOutColors.Background)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = GoOutColors.TextPrimary)
            }
            Column {
                Text(
                    corridor?.corridorName  ?: "Memuat...",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoOutColors.TextPrimary ,
                )
                corridor?.let{
                    Text(it.segmentLabel, style = MaterialTheme.typography.bodySmall, color = GoOutColors.TextSecondary)
                }
            }
        }

        val c = corridor
        if (c == null) {
            Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoOutColors.Primary)
            }
            return@Column
        }

        if (c.polyline.isNotEmpty()) {
            val mid = c.polyline[c.polyline.size / 2]
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(LatLng(mid.latitude, mid.longitude), 14.5f)
            }
            GoogleMap (
                modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 16.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false),
            ) {
                Polyline(
                    points = c.polyline.map { LatLng(it.latitude, it.longitude) },
                    color = c.densityStatus.statusColor(),
                    width = 20f,
                )
            }
        }
        Column (
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(16.dp))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(c.densityStatus.statusColor(), androidx.compose.foundation.shape.CircleShape),
                )
                Spacer(Modifier.size(8.dp))
                StatusBadge(c.densityStatus)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    c.currentSpeed?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "-",
                    fontSize = 48.sp,
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoOutColors.TextPrimary,
                )
                Text(" km/jam", style = MaterialTheme.typography.bodyMedium, color = GoOutColors.TextSecondary)
            }
            Spacer(Modifier.height(12.dp))
            DetailRow("Batas Kecepatan Normal", "40 km/jam")
            DetailRow(
                "Estimasi Waktu Tempuh",
                c.currentSpeed?.takeIf { it > 0 }?.let { speed ->
                    val lengthKm = approximateLengthKm(c.polyline)
                    "~${((lengthKm / speed) * 60).toInt().coerceAtLeast(1)} mnt (${String.format(java.util.Locale.US, "%.1f", lengthKm)} km)"
                } ?: "—",
            )
            DetailRow("Terakhir diperbarui", relativeTime(c.lastUpdated))
            if (c.densityStatus == DensityStatus.MACET) {
                Spacer(Modifier.height(8.dp))
                MacetWarning(c.corridorName)
            }
            if (c.densityStatus == DensityStatus.UNKNOWN) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Belum ada data terbaru untuk koridor ini. Kirim laporan untuk memperbaruinya,",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoOutColors.TextSecondary
                )
            }
        }

        Button(
            onClick = onReport,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoOutColors.Primary),
        ) {
            Icon(Icons.Filled.AddLocationAlt, contentDescription = null)
            Text(" Lapor Kondisi Lalu Lintas")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = GoOutColors.TextSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall, color = GoOutColors.TextPrimary)
    }
}

private fun approximateLengthKm(polyline: List<com.example.goout.domain.model.GeoPoint>): Double {
    var meters = 0.0
    for (i in 0 until polyline.size - 1) {
        meters += com.example.goout.core.utils.GeoUtils.haversineMeters(polyline[i],polyline[i + 1])
    }
    return meters / 1000.0
}