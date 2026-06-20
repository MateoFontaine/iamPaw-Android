package com.example.iampaw.components.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.domain.IPawRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val repository: IPawRepository
) : ViewModel() {

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
