package app.grampsmaterial.feature_home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_network.PersonRepository
import app.grampsmaterial.core_network.models.displayName
import app.grampsmaterial.core_network.models.lifeYears
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
                val homePersonHandle = sessionManager.homePersonHandleFlow.first()
                var usingCachedData = false
                val people = try {
                    personRepository.loadFirstPeoplePage()
                } catch (error: Exception) {
                    Log.w(TAG, "Could not refresh people; using cache", error)
                    usingCachedData = true
                    personRepository.getAllCachedPeople()
                }
                val personCount = people.size
                val birthdays = people.mapNotNull { person ->
                    val date = person.profile?.birth?.date ?: return@mapNotNull null
                    birthdayMonth(date)?.let { month -> Birthday(person.displayName(), date, month) }
                }.sortedBy { it.date }
                val homePerson = personRepository.getCachedPerson(homePersonHandle)?.let { person ->
                    PersonSummary(person.handle, person.displayName(), person.lifeYears())
                }
                _uiState.update { it.copy(
                    username = username,
                    selectedTreeName = selectedTreeName,
                    homePerson = homePerson,
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

    fun setBirthdayMonth(month: Int) {
        _uiState.update { it.copy(birthdayMonth = month.coerceIn(1, 12)) }
    }

    private fun birthdayMonth(date: String): Int? {
        Regex("""(?:^|\D)(0?[1-9]|1[0-2])(?:\D|$)""").find(date)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { return it }
        val names = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
        return names.indexOfFirst { date.contains(it, ignoreCase = true) }.takeIf { it >= 0 }?.plus(1)
    }

    data class Birthday(val displayName: String, val date: String, val month: Int)
    data class PersonSummary(val handle: String, val displayName: String, val lifeYears: String?)

    data class UiState(
        val username: String = "",
        val selectedTreeName: String = "",
        val homePerson: PersonSummary? = null,
        val personCount: Int = 0,
        val birthdays: List<Birthday> = emptyList(),
        val birthdayMonth: Int = java.time.LocalDate.now().monthValue,
        val isUsingCachedData: Boolean = false,
        val recentPeople: List<RecentPersonEntity> = emptyList()
    )
}
