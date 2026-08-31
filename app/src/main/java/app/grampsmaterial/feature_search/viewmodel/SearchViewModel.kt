package app.grampsmaterial.feature_search.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_network.PersonRepository
import app.grampsmaterial.core_network.models.SearchResult
import app.grampsmaterial.core_database.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

private const val TAG = "SearchViewModel"

@HiltViewModel
open class SearchViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    open val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    open fun searchPeople(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                if (query.isBlank()) {
                    throw IllegalArgumentException("Please enter a search term")
                }
                
                val results = personRepository.searchPeopleFromNetwork(query)
                
                _uiState.update { it.copy(
                    isLoading = false,
                    results = results
                ) }
                
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error during search", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Search failed. Please try again."
                ) }
            } catch (e: Exception) {
                Log.e(TAG, "Error during search", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Search error: ${e.localizedMessage}"
                ) }
            }
        }
    }

    open fun refresh() {
        // Implementation for refresh if needed
    }

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val results: List<SearchResult> = emptyList()
    )
}
