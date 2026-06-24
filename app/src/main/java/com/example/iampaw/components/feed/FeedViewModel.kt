package com.example.iampaw.components.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.domain.IPawRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: IPawRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedState(isLoading = true))
    val uiState: StateFlow<FeedState> = _uiState.asStateFlow()

    init {
        observeFeed()
    }

    private fun observeFeed() {
        viewModelScope.launch {
            try {
                repository.refreshFeedIfEmpty()
                repository.syncReportsFromFirestore()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                return@launch
            }

            repository.observeFeed()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { posts ->
                    _uiState.update {
                        it.copy(posts = posts, isLoading = false, errorMessage = null)
                    }
                }
        }
    }
}
