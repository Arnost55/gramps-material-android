package app.grampsmaterial.feature_person.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.PersonRepository
import app.grampsmaterial.core_network.models.DnaMatch
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
                val research = runCatching { loadResearch(person) }.getOrDefault(emptyList())
                val dnaMatches = runCatching { personRepository.getDnaMatches(person.handle) }.getOrDefault(emptyList())
                val media = person.media_list.take(12).map { ref ->
                    MediaPreview(
                        handle = ref.ref,
                        thumbnailUrl = "${sessionManager.serverUrlFlow.first().trimEnd('/')}/api/media/${ref.ref}/thumbnail/256",
                        accessToken = sessionManager.getAccessToken().orEmpty()
                    )
                }
                val relationshipToHome = sessionManager.homePersonHandleFlow.first()
                    .takeIf { it.isNotBlank() && it != person.handle }
                    ?.let { home -> runCatching { personRepository.getRelationship(person.handle, home).relationship_string }.getOrNull() }
                _uiState.value = UiState(
                    person = person,
                    relationships = relationships,
                    isHomePerson = isHomePerson,
                    isBookmarked = isBookmarked,
                    relationshipToHome = relationshipToHome,
                    timeline = timeline,
                    research = research,
                    dnaMatches = dnaMatches,
                    media = media
                )
            } catch (_: Exception) {
                _uiState.update {
                    if (it.person != null) it.copy(isLoading = false, isStale = true)
                    else it.copy(isLoading = false, error = "Unable to load this person. Check your connection and try again.")
                }
            }
        }
    }

    fun updateName(firstName: String, surname: String) = viewModelScope.launch {
        val person = _uiState.value.person ?: return@launch
        _uiState.update { it.copy(isSaving = true, notice = null) }
        try {
            val updated = personRepository.updatePersonName(person.handle, firstName.trim(), surname.trim())
            _uiState.update { it.copy(person = updated, isSaving = false, notice = "Changes saved") }
        } catch (_: Exception) {
            _uiState.update { it.copy(isSaving = false, notice = "Could not save changes. Your original record was not modified.") }
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

    private suspend fun loadResearch(person: GrampsPerson): List<ResearchEntry> {
        val citations = person.citation_list.mapNotNull { handle ->
            runCatching { personRepository.getCitation(handle) }.getOrNull()
        }
        val citationEntries = citations.map { citation ->
            val source = citation.source ?: citation.source_handle?.let { handle ->
                runCatching { personRepository.getSource(handle) }.getOrNull()
            }
            ResearchEntry(
                title = source?.title ?: source?.abbrev ?: "Citation",
                detail = listOfNotNull(source?.author, citation.page?.let { "p. $it" }).joinToString(" · ").ifBlank { null },
                kind = "Citation"
            )
        }
        val noteEntries = person.note_list.mapNotNull { handle ->
            runCatching { personRepository.getNote(handle) }.getOrNull()
        }.map { note ->
            ResearchEntry(
                title = "Research note",
                detail = (note.text ?: note.styledtext)?.takeIf(String::isNotBlank),
                kind = "Note"
            )
        }
        return citationEntries + noteEntries
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

    data class ResearchEntry(val title: String, val detail: String?, val kind: String)

    data class MediaPreview(val handle: String, val thumbnailUrl: String, val accessToken: String)

    data class Relationships(
        val parents: List<RelatedPerson> = emptyList(),
        val partners: List<RelatedPerson> = emptyList(),
        val children: List<RelatedPerson> = emptyList()
    )

    data class UiState(
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val isStale: Boolean = false,
        val error: String? = null,
        val person: GrampsPerson? = null,
        val relationships: Relationships = Relationships(),
        val isHomePerson: Boolean = false,
        val isBookmarked: Boolean = false,
        val relationshipToHome: String? = null,
        val timeline: List<TimelineEvent> = emptyList(),
        val research: List<ResearchEntry> = emptyList(),
        val dnaMatches: List<DnaMatch> = emptyList(),
        val media: List<MediaPreview> = emptyList(),
        val notice: String? = null
    )
}
