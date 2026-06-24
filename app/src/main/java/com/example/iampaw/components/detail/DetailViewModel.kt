package com.example.iampaw.components.detail

import androidx.lifecycle.ViewModel
import com.example.iampaw.domain.IPawRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: IPawRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailState())
    val uiState: StateFlow<DetailState> = _uiState.asStateFlow()

    init {
        loadDogDetails()
    }

    private fun loadDogDetails() {
        _uiState.value = repository.getDogDetail("1")
    }
}
