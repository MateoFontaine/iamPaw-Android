package com.example.iampaw.components.detail

import androidx.lifecycle.ViewModel
import com.example.iampaw.data.PawMockDataSource
import com.example.iampaw.data.PawRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DetailViewModel : ViewModel() {

    // 1. Instanciamos el repositorio con nuestra fuente de datos de prueba
    private val repository = PawRepository(PawMockDataSource())

    private val _uiState = MutableStateFlow(DetailState())
    val uiState: StateFlow<DetailState> = _uiState.asStateFlow()

    init {
        loadDogDetails()
    }

    private fun loadDogDetails() {
        _uiState.value = repository.getDogDetail("1")
    }
}