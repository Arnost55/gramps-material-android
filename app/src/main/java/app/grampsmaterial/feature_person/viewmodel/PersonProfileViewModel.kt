package app.grampsmaterial.feature_person.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.PersonRepository
import app.grampsmaterial.core_network.models.GrampsPerson
import app.grampsmaterial.core_network.models.TimelineEvent
import app.grampsmaterial.core_network.models.displayName
import app.grampsmaterial.core_network.models.lifeYears
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonProfileViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadPerson(personHandle: String) {
        viewModelScope.launch {
            if (personHandle.isBlank()) {
                _uiState.value = UiState(error = "Person handle is required")
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            val cached = personRepository.getCachedPerson(personHandle)
            if (cached != null) _uiState.update { it.copy(person = cached, isLoading = true, isStale = true) }
            try {
                val person = personRepository.getPersonFromNetwork(personHandle)
                val isHomePerson = sessionManager.homePersonHandleFlow.first() == person.handle
                val relationships = loadRelationships(person)
                val isBookmarked = runCatching { personRepository.getPeopleBookmarks().contains(person.handle) }.getOrDefault(false)
                val timeline = runCatching { personRepository.getPersonTimeline(person.handle) }.getOrDefault(emptyList())
                val relationshipToHome = sessionManager.homePersonHandleFlow.first()
                    .takeIf { it.isNotBlank() && it != person.handle }
                    ?.let { home -> runCatching { personRepository.getRelationship(person.handle, home).relationship_string }.getOrNull() }
                _uiState.value = UiState(
                    person = person,
                    relationships = relationships,
                    isHomePerson = isHomePerson,
                    isBookmarked = isBookmarked,
                    relationshipToHome = relationshipToHome,
                    timeline = timeline
                )
            } catch (_: Exception) {
                _uiState.update {
                    if (it.person != null) it.copy(isLoading = false, isStale = true)
                    else it.copy(isLoading = false, error = "Unable to load this person. Check your connection and try again.")
                }
            }
        }
    }

    fun toggleBookmark() = viewModelScope.launch {
        val person = _uiState.value.person ?: return@launch
        val target = !_uiState.value.isBookmarked
        try {
            personRepository.setPersonBookmarked(person.handle, target)
            _uiState.update { it.copy(isBookmarked = target, notice = null) }
        } catch (_: Exception) {
            _uiState.update { it.copy(notice = "Unable to update the server bookmark.") }
        }
    }

    fun setAsHomePerson() = viewModelScope.launch {
        val person = _uiState.value.person ?: return@launch
        sessionManager.saveHomePersonHandle(person.handle)
        _uiState.update { it.copy(isHomePerson = true) }
    }

    private suspend fun loadRelationships(person: GrampsPerson): Relationships {
        val parentFamilies = person.parent_family_list.map { personRepository.getFamilyFromNetwork(it) }
        val partnerFamilies = person.family_list.map { personRepository.getFamilyFromNetwork(it) }
        val parents = parentFamilies.flatMap { listOfNotNull(it.father_handle, it.mother_handle) }.distinct()
        val partners = partnerFamilies.flatMap { listOfNotNull(it.father_handle, it.mother_handle) }
            .filterNot { it == person.handle }.distinct()
        val children = partnerFamilies.flatMap { family -> family.child_ref_list.map { it.ref } }.distinct()
        suspend fun summaries(handles: List<String>): List<RelatedPerson> = handles.map { handle ->
            val related = personRepository.getPersonForGraph(handle)
            RelatedPerson(handle = related.handle, displayName = related.displayName(), lifeYears = related.lifeYears())
        }
        return Relationships(summaries(parents), summaries(partners), summaries(children))
    }

    data class RelatedPerson(val handle: String, val displayName: String, val lifeYears: String?)

    data class Relationships(
        val parents: List<RelatedPerson> = emptyList(),
        val partners: List<RelatedPerson> = emptyList(),
        val children: List<RelatedPerson> = emptyList()
    )

    data class UiState(
        val isLoading: Boolean = false,
        val isStale: Boolean = false,
        val error: String? = null,
        val person: GrampsPerson? = null,
        val relationships: Relationships = Relationships(),
        val isHomePerson: Boolean = false,
        val isBookmarked: Boolean = false,
        val relationshipToHome: String? = null,
        val timeline: List<TimelineEvent> = emptyList(),
        val notice: String? = null
    )
}
