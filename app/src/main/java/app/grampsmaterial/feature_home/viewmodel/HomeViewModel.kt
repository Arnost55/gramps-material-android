package app.grampsmaterial.feature_home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_network.PersonRepository
import app.grampsmaterial.core_network.models.displayName
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_database.RecentPersonEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    open val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadState()
        viewModelScope.launch {
            personRepository.observeRecentPeople().collect { recent ->
                _uiState.update { it.copy(recentPeople = recent) }
            }
        }
    }

    private fun loadState() {
        viewModelScope.launch {
            try {
                val username = sessionManager.usernameFlow.first()
                val selectedTreeName = sessionManager.selectedTreeNameFlow.first()
                var usingCachedData = false
                val people = try {
                    personRepository.loadFirstPeoplePage()
                } catch (error: Exception) {
                    Log.w(TAG, "Could not refresh people; using cache", error)
                    usingCachedData = true
                    personRepository.getAllCachedPeople()
                }
                val personCount = people.size
                val currentMonth = java.time.LocalDate.now().monthValue
                val birthdays = people.mapNotNull { person ->
                    val date = person.profile?.birth?.date ?: return@mapNotNull null
                    if (Regex("""(?:^|\\D)0?$currentMonth(?:\\D|$)""").containsMatchIn(date)) {
                        Birthday(person.displayName(), date)
                    } else {
                        null
                    }
                }.sortedBy { it.date }
                _uiState.update { it.copy(
                    username = username,
                    selectedTreeName = selectedTreeName,
                    personCount = personCount,
                    birthdays = birthdays,
                    isUsingCachedData = usingCachedData
                ) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading home state", e)
            }
        }
    }

    open fun refresh() {
        loadState()
    }

    data class Birthday(val displayName: String, val date: String)

    data class UiState(
        val username: String = "",
        val selectedTreeName: String = "",
        val personCount: Int = 0,
        val birthdays: List<Birthday> = emptyList(),
        val isUsingCachedData: Boolean = false,
        val recentPeople: List<RecentPersonEntity> = emptyList()
    )
}
