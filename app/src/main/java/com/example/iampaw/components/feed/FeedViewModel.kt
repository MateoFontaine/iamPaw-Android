package com.example.iampaw.components.feed

import androidx.lifecycle.ViewModel
import com.example.iampaw.domain.IPawRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: IPawRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedState())
    val uiState: StateFlow<FeedState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    private fun loadFeed() {
        _uiState.value = FeedState(
            posts = repository.getFeedDogs()
        )
    }
}
