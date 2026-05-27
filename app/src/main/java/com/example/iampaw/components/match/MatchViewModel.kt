package com.example.iampaw.components.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.data.PawMockDataSource
import com.example.iampaw.data.PawRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchViewModel : ViewModel() {

    // 1. Instanciamos el repositorio con nuestra fuente de datos
    private val repository = PawRepository(PawMockDataSource())

    private val _uiState = MutableStateFlow(MatchState())
    val uiState: StateFlow<MatchState> = _uiState.asStateFlow()

    init {
        startScanningSimulation()
    }

    private fun startScanningSimulation() {
        viewModelScope.launch {


            delay(3500)


            _uiState.value = MatchState(
                isScanning = false,
                matches = repository.getMatchedDogs()
            )
        }
    }
}