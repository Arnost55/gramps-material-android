package app.grampsmaterial.feature_tree.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.PersonRepository
import app.grampsmaterial.core_network.models.GrampsPerson
import app.grampsmaterial.feature_tree.AncestorGraphBuilder
import app.grampsmaterial.feature_tree.TreeLayout
import app.grampsmaterial.feature_tree.TreeLayoutEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import javax.inject.Inject

@HiltViewModel
class TreeViewerViewModel @Inject constructor(
    private val people: PersonRepository,
    private val session: SessionManager
) : ViewModel() {
    private val graphBuilder = AncestorGraphBuilder()
    private val layoutEngine = TreeLayoutEngine()
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    fun load(generations: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
        val root = session.homePersonHandleFlow.first()
        if (root.isBlank()) {
            _state.value = UiState(message = "Open a person profile first to choose a tree root.")
            return@launch
        }
        _state.value = UiState(isLoading = true, generations = generations)
        try {
            val resolved = linkedMapOf<String, GrampsPerson>()
            suspend fun loadPerson(handle: String): GrampsPerson {
                return resolved[handle] ?: people.getPersonForGraph(handle).also { resolved[handle] = it }
            }
            val parentMap = mutableMapOf<String, List<String>>()
            var frontier = listOf(root)
            repeat(generations - 1) {
                val next = mutableListOf<String>()
                frontier.forEach { handle ->
                    val current = loadPerson(handle)
                    val parents = current.parent_family_list.flatMap { familyHandle ->
                        val family = people.getFamilyFromNetwork(familyHandle)
                        listOfNotNull(family.father_handle, family.mother_handle)
                    }.distinct()
                    parentMap[handle] = parents
                    next += parents
                }
                frontier = next.distinct()
                if (frontier.isEmpty()) return@repeat
            }
            val graph = graphBuilder.build(root, generations) { parentMap[it].orEmpty() }
            graph.generations.flatten().forEach { loadPerson(it) }
            _state.value = UiState(
                generations = generations,
                graph = graph.generations,
                edges = graph.edges,
                layout = layoutEngine.layout(graph.generations),
                people = resolved,
                rootHandle = root
            )
        } catch (_: Exception) {
            coroutineContext.ensureActive()
            _state.value = UiState(generations = generations, message = "Unable to load this ancestor tree. Check your connection and try again.")
        }
        }
    }

    data class UiState(
        val isLoading: Boolean = false,
        val generations: Int = 4,
        val rootHandle: String? = null,
        val graph: List<List<String>> = emptyList(),
        val edges: Set<app.grampsmaterial.feature_tree.TreeEdge> = emptySet(),
        val layout: TreeLayout? = null,
        val people: Map<String, GrampsPerson> = emptyMap(),
        val message: String? = null
    )
}
