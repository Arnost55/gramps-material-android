package app.grampsmaterial.feature_search.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_network.PersonRepository
import app.grampsmaterial.core_network.models.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

private const val TAG = "SearchViewModel"

@HiltViewModel
open class SearchViewModel @Inject constructor(
    private val personRepository: PersonRepository
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
                    results = results,
                    isOffline = false
                ) }
                
            } catch (e: Exception) {
                coroutineContext.ensureActive()
                Log.w(TAG, "Network search unavailable; checking cache", e)
                val cached = personRepository.searchCachedPeople(query)
                _uiState.update {
                    if (cached.isNotEmpty()) it.copy(isLoading = false, results = cached, isOffline = true)
                    else it.copy(isLoading = false, results = emptyList(), isOffline = true,
                        error = "Offline and no matching people are cached on this device.")
                }
            }
        }
    }

    /** Shows the full people list when no search term is supplied. */
    open fun loadAllPeople() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = UiState(isLoading = true)
            try {
                val people = personRepository.loadAllPeopleFromNetwork()
                _uiState.value = UiState(
                    results = people.map { person ->
                        SearchResult(handle = person.handle, object_type = "person", `object` = person)
                    }
                )
            } catch (e: Exception) {
                coroutineContext.ensureActive()
                val cached = personRepository.getAllCachedPeople()
                _uiState.value = UiState(
                    results = cached.map { person ->
                        SearchResult(handle = person.handle, object_type = "person", `object` = person)
                    },
                    isOffline = true,
                    error = if (cached.isEmpty()) "Offline and no people are cached on this device." else null
                )
            }
        }
    }

    open fun clearSearch() = loadAllPeople()

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val results: List<SearchResult> = emptyList(),
        val isOffline: Boolean = false
    )
}
