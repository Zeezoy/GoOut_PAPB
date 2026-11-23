package com.example.goout.feature.corridor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goout.domain.model.Corridor
import com.example.goout.domain.usecase.GetCorridorDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CorridorDetailViewModel @Inject constructor(
    getCorridorDetail: GetCorridorDetailUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val corridorId: String = savedStateHandle["corridorId"] ?: ""

    val corridor: StateFlow<Corridor?> = getCorridorDetail(corridorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
