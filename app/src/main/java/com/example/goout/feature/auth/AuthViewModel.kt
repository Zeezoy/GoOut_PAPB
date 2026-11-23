package com.example.goout.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthFormState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)

    private val _form = MutableStateFlow(AuthFormState())
    val form: StateFlow<AuthFormState> = _form.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _form.value = AuthFormState(error = "Email dan kata sandi wajib diisi.")
            return
        }
        _form.value = AuthFormState(isLoading = true)
        viewModelScope.launch {
            authRepository.login(email.trim(), password)
                .onSuccess { _form.value = AuthFormState(success = true) }
                .onFailure { _form.value = AuthFormState(error = it.message ?: "Login gagal.") }
        }
    }

    fun register(name: String, email: String, password: String) {
        when {
            name.isBlank() -> { _form.value = AuthFormState(error = "Nama wajib diisi."); return }
            email.isBlank() -> { _form.value = AuthFormState(error = "Email wajib diisi."); return }
            password.length < 6 -> { _form.value = AuthFormState(error = "Kata sandi minimal 6 karakter."); return }
        }
        _form.value = AuthFormState(isLoading = true)
        viewModelScope.launch {
            authRepository.register(name.trim(), email.trim(), password)
                .onSuccess { _form.value = AuthFormState(success = true) }
                .onFailure { _form.value = AuthFormState(error = it.message ?: "Registrasi gagal.") }
        }
    }

    fun clearForm() { _form.value = AuthFormState() }
}
