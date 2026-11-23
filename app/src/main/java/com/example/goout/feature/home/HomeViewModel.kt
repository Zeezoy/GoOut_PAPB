package com.example.goout.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goout.core.network.ConnectivityObserver
import com.example.goout.domain.model.Corridor
import com.example.goout.domain.model.DensityStatus
import com.example.goout.domain.repository.AuthRepository
import com.example.goout.domain.repository.AuthState
import com.example.goout.domain.usecase.ObserveCorridorsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val corridors: List<Corridor> = emptyList(),
    val isOnline: Boolean = true,
    val macetCount: Int = 0,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeCorridors: ObserveCorridorsUseCase,
    connectivity: ConnectivityObserver,
    authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        observeCorridors(),
        connectivity.observe(),
        authRepository.authState,
    ) { corridors, online, auth ->
        HomeUiState(
            corridors = corridors,
            isOnline = online,
            macetCount = corridors.count { it.densityStatus == DensityStatus.MACET },
            isAuthenticated = auth is AuthState.Authenticated,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
