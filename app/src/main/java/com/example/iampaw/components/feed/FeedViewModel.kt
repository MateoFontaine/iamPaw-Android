package com.example.iampaw.components.feed

import androidx.lifecycle.ViewModel
import com.example.iampaw.data.PawMockDataSource
import com.example.iampaw.data.PawRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FeedViewModel : ViewModel() {

    // 1. Instanciamos el repositorio con nuestra fuente de datos
    private val repository = PawRepository(PawMockDataSource())

    private val _uiState = MutableStateFlow(FeedState())
    val uiState: StateFlow<FeedState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    private fun loadFeed() {
        // 2. Adiós a la lista hardcodeada. Le pedimos los datos al repositorio.
        _uiState.value = FeedState(
            posts = repository.getFeedDogs()
        )
    }
}