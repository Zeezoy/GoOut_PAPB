package com.example.goout.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goout.core.designsystem.GoOutColors
import com.example.goout.domain.model.DensityStatus
import com.example.goout.feature.location.LocationBanner

/** Layar "Lapor" - pelaporan kondisi lalu lintas 1-tap. */
@Composable
fun ReportScreen(
    onBackToMap: () -> Unit,
    onLogin: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(GoOutColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Lapor Kondisi Jalan", style = MaterialTheme.typography.titleLarge, color = GoOutColors.TextPrimary)
        Text(
            "Pilih kondisi arus kendaraan saat ini. Data Anda memperbarui peta warga Malang secara real-time.",
            style = MaterialTheme.typography.bodySmall,
            color = GoOutColors.TextSecondary,
        )
        Spacer(Modifier.height(16.dp))

        LocationBanner(state.location, onRetry = viewModel::refreshLocation)

        if (!isAuthenticated) {
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(GoOutColors.PrimaryContainer, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Silakan login untuk mengirim laporan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoOutColors.Primary,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onLogin) { Text("Masuk", color = GoOutColors.Primary) }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "PILIH TINGKAT KEPADATAN",
            style = MaterialTheme.typography.labelMedium,
            color = GoOutColors.TextPrimary,
        )
        Spacer(Modifier.height(10.dp))

        ReportOption(
            status = DensityStatus.LANCAR,
            speedLabel = "≥ 35 km/jam",
            description = "Arus lancar tanpa hambatan berarti",
            selected = state.selected == DensityStatus.LANCAR,
            enabled = !state.isSubmitting,
            onClick = { viewModel.select(DensityStatus.LANCAR) },
        )
        Spacer(Modifier.height(10.dp))
        ReportOption(
            status = DensityStatus.MERAYAP,
            speedLabel = "15 – 34.9 km/jam",
            description = "Kecepatan berkurang, antrean lampu merah / persimpangan",
            selected = state.selected == DensityStatus.MERAYAP,
            enabled = !state.isSubmitting,
            onClick = { viewModel.select(DensityStatus.MERAYAP) },
        )
        Spacer(Modifier.height(10.dp))
        ReportOption(
            status = DensityStatus.MACET,
            speedLabel = "< 15 km/jam",
            description = "Kendaraan berhenti lama, hambatan berat atau padat total",
            selected = state.selected == DensityStatus.MACET,
            enabled = !state.isSubmitting,
            onClick = { viewModel.select(DensityStatus.MACET) },
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = viewModel::submit,
            enabled = state.selected != null && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoOutColors.Primary),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(Modifier.size(20.dp), color = GoOutColors.OnPrimary, strokeWidth = 2.dp)
                Text("  Mengirim…")
            } else {
                Text("Kirim Laporan Kondisi")
            }
        }
        TextButton(onClick = onBackToMap, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Batal", color = GoOutColors.TextSecondary)
        }
    }
}
