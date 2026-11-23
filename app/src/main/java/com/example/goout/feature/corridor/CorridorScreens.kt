package com.example.goout.feature.corridor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.goout.feature.home.HomeViewModel

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
    // TODO(Bastian)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("TODO Bastian: daftar koridor")
    }
}

@Composable
fun CorridorDetailScreen(
    onBack: () -> Unit,
    onReport: () -> Unit,
    viewModel: CorridorDetailViewModel = hiltViewModel(),
) {
    // TODO(Bastian)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("TODO Bastian: detail koridor")
    }
}
