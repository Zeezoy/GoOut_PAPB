package com.example.goout.feature.home

import android.Manifest
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goout.R
import com.example.goout.core.designsystem.CorridorCard
import com.example.goout.core.designsystem.DensityLegend
import com.example.goout.core.designsystem.GoOutColors
import com.example.goout.core.designsystem.statusColor
import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.DensityStatus
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/** Center of the three monitored corridors in Malang. */
private val MalangCenter = LatLng(-7.9465, 112.6235)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onCorridorClick: (String) -> Unit,
    onReportClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    Column(
        Modifier
            .fillMaxSize()
            .background(GoOutColors.Background)
            .verticalScroll(rememberScrollState()),
    ) {
        HomeHeader(isOnline = state.isOnline, onProfileClick = onProfileClick)
        StatusStrip(state)

        // [OWNER: Fevo] [FUNGSI 4] Blok peta Home.
        // Diharapkan: Box tinggi 400.dp berisi GoogleMap (kamera di MalangCenter, zoom 13,5,
        // isMyLocationEnabled dari izin lokasi), satu Polyline per koridor berwarna
        // corridor.densityStatus.statusColor() dan bisa diklik (onCorridorClick),
        // DensityLegend di kiri atas, tombol "Lapor" (onReportClick) di kanan bawah,
        // dan LocationPermissionButton (milik Radit) bila izin belum diberikan.
        // TODO(Fevo)
        Box(
            Modifier
                .fillMaxWidth()
                .height(400.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("TODO Fevo: peta Home")
        }

        // Bottom sheet style corridor status panel (Figma: "Status Koridor Terpantau")
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Color.White,
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 40.dp, height = 4.dp)
                    .background(GoOutColors.Outline, RoundedCornerShape(999.dp)),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Status Koridor Terpantau",
                style = MaterialTheme.typography.titleMedium,
                color = GoOutColors.TextPrimary,
            )
            Text(
                "Berdasarkan crowdsource telemetry & laporan warga",
                style = MaterialTheme.typography.bodySmall,
                color = GoOutColors.TextSecondary,
            )
            Spacer(Modifier.height(12.dp))
            if (state.corridors.isEmpty()) {
                Text(
                    "Belum ada data koridor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoOutColors.TextSecondary,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                state.corridors.forEach { corridor ->
                    CorridorCard(
                        corridor = corridor,
                        onClick = { onCorridorClick(corridor.corridorId) },
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
                InsightNotice(state.corridors)
            }
        }
    }
}

@Composable
private fun HomeHeader(isOnline: Boolean, onProfileClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.goout_logo),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)),
            )
            Column(Modifier.padding(start = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Go Out", style = MaterialTheme.typography.titleMedium, color = GoOutColors.TextPrimary)
                    Box(
                        Modifier
                            .padding(start = 4.dp)
                            .background(GoOutColors.ChipNeutral, RoundedCornerShape(999.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text("Kota Malang", fontSize = 10.sp, color = GoOutColors.TextSecondary)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(
                                if (isOnline) GoOutColors.LiveGreen else GoOutColors.OnErrorContainer,
                                CircleShape,
                            ),
                    )
                    Text(
                        if (isOnline) "  Live Sync · Peta Lalu Lintas" else "  Offline · Data tersimpan",
                        fontSize = 12.sp,
                        color = if (isOnline) GoOutColors.LiveGreen else GoOutColors.OnErrorContainer,
                    )
                }
            }
        }
        IconButton(onClick = onProfileClick) {
            Icon(Icons.Filled.Person, contentDescription = "Profil", tint = GoOutColors.TextSecondary)
        }
    }
}

@Composable
private fun StatusStrip(state: HomeUiState) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .background(GoOutColors.SurfaceCard, RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                "${state.corridors.size} Koridor Aktif",
                fontSize = 11.sp,
                color = GoOutColors.TextSecondary,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (state.macetCount > 0) {
                Row(
                    Modifier
                        .background(GoOutColors.ErrorContainer, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = GoOutColors.OnErrorContainer,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        " ${state.macetCount} Macet",
                        fontSize = 11.sp,
                        color = GoOutColors.OnErrorContainer,
                    )
                }
            }
            if (!state.isOnline) {
                Icon(
                    Icons.Filled.CloudOff,
                    contentDescription = "Offline",
                    tint = GoOutColors.TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun InsightNotice(corridors: List<Corridor>) {
    val worst = corridors
        .filter { it.densityStatus == DensityStatus.MACET }
        .minByOrNull { it.currentSpeed ?: Double.MAX_VALUE }
    val text = when {
        worst != null ->
            "Titik tersendat terpusat di ${worst.corridorName} (${worst.segmentLabel}) " +
                "dengan kecepatan rata-rata ${worst.currentSpeed?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "?"} km/h."
        corridors.all { it.densityStatus == DensityStatus.UNKNOWN } ->
            "Belum ada data lalu lintas terbaru. Aktifkan telemetri atau kirim laporan untuk memperbarui peta."
        else -> "Semua koridor terpantau dalam kondisi terkendali saat ini."
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .background(GoOutColors.ChipNeutral.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = GoOutColors.TextPrimary)
    }
}
