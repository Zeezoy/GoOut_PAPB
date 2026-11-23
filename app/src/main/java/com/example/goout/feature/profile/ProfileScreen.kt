package com.example.goout.feature.profile

import android.Manifest
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goout.core.designsystem.GoOutColors
import com.example.goout.domain.repository.AuthState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/** Screen "Profil / Pengaturan". */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val notifPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

    Column(
        Modifier
            .fillMaxSize()
            .background(GoOutColors.Background)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = GoOutColors.TextPrimary)
            }
            Text("Profil & Pengaturan", style = MaterialTheme.typography.titleMedium, color = GoOutColors.TextPrimary)
        }

        // Account card
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            when (val auth = state.authState) {
                is AuthState.Authenticated -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(48.dp).background(GoOutColors.PrimaryContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = GoOutColors.Primary)
                        }
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(
                                auth.user.displayName.ifBlank { "Pengguna Go Out" },
                                style = MaterialTheme.typography.titleSmall,
                                color = GoOutColors.TextPrimary,
                            )
                            Text(auth.user.email, style = MaterialTheme.typography.bodySmall, color = GoOutColors.TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = viewModel::logout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = GoOutColors.OnErrorContainer)
                        Text("  Keluar", color = GoOutColors.OnErrorContainer)
                    }
                }
                AuthState.Guest -> {
                    Text("Mode Tamu", style = MaterialTheme.typography.titleSmall, color = GoOutColors.TextPrimary)
                    Text(
                        "Anda dapat melihat peta, tetapi perlu login untuk mengirim laporan dan telemetri.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoOutColors.TextSecondary,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onLogin, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp)) {
                        Text("Masuk / Daftar", color = GoOutColors.Primary)
                    }
                }
                AuthState.Loading -> Text("Memuat…", style = MaterialTheme.typography.bodySmall, color = GoOutColors.TextSecondary)
            }
        }

        // Telemetry settings
        SettingsCard(title = "Telemetri Pasif") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Bagikan kecepatan kendaraan secara otomatis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoOutColors.TextPrimary,
                    )
                    Text(
                        "Opt-in. Lokasi hanya dikumpulkan saat fitur ini aktif, dengan notifikasi yang selalu terlihat. " +
                            "Data kecepatan dikirim anonim per koridor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoOutColors.TextSecondary,
                    )
                }
                Switch(
                    checked = state.telemetryEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && !locationPermission.status.isGranted) {
                            locationPermission.launchPermissionRequest()
                        } else {
                            if (enabled && !notifPermission.status.isGranted) notifPermission.launchPermissionRequest()
                            viewModel.setTelemetryEnabled(enabled)
                        }
                    },
                    enabled = state.authState is AuthState.Authenticated,
                    colors = SwitchDefaults.colors(checkedTrackColor = GoOutColors.Primary),
                )
            }
            if (state.authState !is AuthState.Authenticated) {
                Text(
                    "Login diperlukan untuk mengaktifkan telemetri.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoOutColors.OnMerayapContainer,
                )
            }
            StatusRow("Izin lokasi", if (locationPermission.status.isGranted) "Diberikan" else "Belum diberikan")
            if (!locationPermission.status.isGranted) {
                TextButton(onClick = { locationPermission.launchPermissionRequest() }) {
                    Text("Minta izin lokasi", color = GoOutColors.Primary, fontSize = 12.sp)
                }
            }
        }

        // Sync status
        SettingsCard(title = "Sinkronisasi") {
            StatusRow("Telemetri menunggu", "${state.pendingTelemetry}")
            StatusRow("Laporan menunggu", "${state.pendingReports}")
            Text(
                "Data tertunda akan dikirim otomatis saat koneksi internet tersedia.",
                style = MaterialTheme.typography.bodySmall,
                color = GoOutColors.TextSecondary,
            )
        }

        // Developer: mock generator (debug builds only)
        if (state.mockAvailable) {
            SettingsCard(title = "Pengembang — Mock Traffic") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Generator lalu lintas simulasi (5–50 km/h, 10 dtk)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoOutColors.TextPrimary,
                        )
                        Text(
                            "Hanya build debug. Data simulasi memperbarui status koridor untuk demo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoOutColors.TextSecondary,
                        )
                    }
                    Switch(
                        checked = state.mockRunning,
                        onCheckedChange = { viewModel.toggleMockGenerator() },
                        colors = SwitchDefaults.colors(checkedTrackColor = GoOutColors.Merayap),
                    )
                }
            }
        }

        SettingsCard(title = "Tentang Aplikasi") {
            Text(
                "Go Out — sistem pemantauan kepadatan koridor jalan Kota Malang berbasis crowdsourcing. " +
                    "Proyek akhir Pengembangan Aplikasi Perangkat Bergerak, terinspirasi SISRI (Surabaya).",
                style = MaterialTheme.typography.bodySmall,
                color = GoOutColors.TextSecondary,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = GoOutColors.TextSecondary)
        content()
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = GoOutColors.TextSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall, color = GoOutColors.TextPrimary)
    }
}
