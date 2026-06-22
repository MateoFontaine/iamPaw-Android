package com.example.iampaw.components.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iampaw.domain.IPawRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: IPawRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedState(isLoading = true))
    val uiState: StateFlow<FeedState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        observeFeed()
    }

    private fun observeFeed() {
        viewModelScope.launch {
            try {
                repository.refreshFeedIfEmpty()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                return@launch
            }

            searchQuery
                .flatMapLatest { query -> repository.observeFeed(query) }
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

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query, isLoading = true) }
    }
}
