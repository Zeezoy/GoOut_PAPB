package com.example.goout.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goout.R
import com.example.goout.core.designsystem.GoOutColors
import com.example.goout.domain.repository.AuthState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onContinue: () -> Unit,
    onLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                delay(600)
                onContinue()
            }
            AuthState.Guest -> {
                delay(900)
                onLogin()
            }
            AuthState.Loading -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoOutColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.goout_logo),
            contentDescription = "Go Out",
            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(20.dp)),
        )
        Spacer(Modifier.height(16.dp))
        Text("Go Out", style = MaterialTheme.typography.headlineSmall, color = GoOutColors.TextPrimary)
        Text(
            "Pantauan kepadatan koridor Kota Malang",
            style = MaterialTheme.typography.bodySmall,
            color = GoOutColors.TextSecondary,
        )
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator(color = GoOutColors.Primary)
    }
}

@Composable
fun LoginScreen(
    onSuccess: () -> Unit,
    onRegister: () -> Unit,
    onGuest: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(form.success) {
        if (form.success) {
            viewModel.clearForm()
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoOutColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.goout_logo),
            contentDescription = null,
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)),
        )
        Spacer(Modifier.height(12.dp))
        Text("Masuk ke Go Out", style = MaterialTheme.typography.titleLarge, color = GoOutColors.TextPrimary)
        Text(
            "Login untuk mengirim laporan & telemetri",
            style = MaterialTheme.typography.bodySmall,
            color = GoOutColors.TextSecondary,
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Kata sandi") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        form.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = GoOutColors.OnErrorContainer, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { viewModel.login(email, password) },
            enabled = !form.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoOutColors.Primary),
        ) {
            if (form.isLoading) {
                CircularProgressIndicator(Modifier.size(20.dp), color = GoOutColors.OnPrimary, strokeWidth = 2.dp)
            } else {
                Text("Masuk")
            }
        }
        TextButton(onClick = { viewModel.clearForm(); onRegister() }) {
            Text("Belum punya akun? Daftar", color = GoOutColors.Primary)
        }
        TextButton(onClick = { viewModel.clearForm(); onGuest() }) {
            Text("Lanjut sebagai tamu (hanya lihat peta)", color = GoOutColors.TextSecondary)
        }
    }
}

@Composable
fun RegisterScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(form.success) {
        if (form.success) {
            viewModel.clearForm()
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoOutColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Buat Akun", style = MaterialTheme.typography.titleLarge, color = GoOutColors.TextPrimary)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Kata sandi (min. 6 karakter)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        form.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = GoOutColors.OnErrorContainer, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { viewModel.register(name, email, password) },
            enabled = !form.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoOutColors.Primary),
        ) {
            if (form.isLoading) {
                CircularProgressIndicator(Modifier.size(20.dp), color = GoOutColors.OnPrimary, strokeWidth = 2.dp)
            } else {
                Text("Daftar")
            }
        }
        TextButton(onClick = { viewModel.clearForm(); onBack() }) {
            Text("Kembali", color = GoOutColors.TextSecondary)
        }
    }
}
