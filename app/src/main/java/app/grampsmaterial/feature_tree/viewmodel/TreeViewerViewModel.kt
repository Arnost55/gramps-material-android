package app.grampsmaterial.feature_tree.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.PersonRepository
import app.grampsmaterial.core_network.models.GrampsPerson
import app.grampsmaterial.feature_tree.RelationshipGraphBuilder
import app.grampsmaterial.feature_tree.TreeMode
import app.grampsmaterial.feature_tree.PositionedNode
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
    private val graphBuilder = RelationshipGraphBuilder()
    private val layoutEngine = TreeLayoutEngine()
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    fun load(generations: Int, mode: TreeMode = TreeMode.ANCESTORS) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
        val root = session.homePersonHandleFlow.first()
        if (root.isBlank()) {
            _state.value = UiState(message = "Open a person profile first to choose a tree root.")
            return@launch
        }
        _state.value = UiState(isLoading = true, generations = generations, mode = mode)
        try {
            val resolved = people.getAllCachedPeople().associateByTo(linkedMapOf()) { it.handle }
            suspend fun loadPerson(handle: String): GrampsPerson =
                resolved[handle] ?: people.getPersonForGraph(handle).also { resolved[handle] = it }

            loadPerson(root)
            val families = people.getAllCachedFamilies().ifEmpty { people.loadAllFamiliesFromNetwork() }
            val parentMap = mutableMapOf<String, MutableList<String>>()
            val childMap = mutableMapOf<String, MutableList<String>>()
            families.forEach { family ->
                val parents = listOfNotNull(family.father_handle, family.mother_handle)
                family.child_ref_list.forEach { child ->
                    parentMap.getOrPut(child.ref) { mutableListOf() }.addAll(parents)
                    parents.forEach { parent -> childMap.getOrPut(parent) { mutableListOf() }.add(child.ref) }
                }
            }
            val graph = graphBuilder.build(
                root = root,
                mode = mode,
                maxDepth = if (mode == TreeMode.FLEX || mode == TreeMode.RADIAL) Int.MAX_VALUE else generations,
                parentsFor = { parentMap[it].orEmpty().distinct() },
                childrenFor = { childMap[it].orEmpty().distinct() }
            )
            graph.generations.flatten().forEach { loadPerson(it) }
            _state.value = UiState(
                generations = generations,
                mode = mode,
                graph = graph.generations,
                edges = graph.edges,
                layout = if (mode == TreeMode.RADIAL) radialLayout(graph.generations) else layoutEngine.layout(graph.generations),
                people = resolved,
                rootHandle = root
            )
        } catch (_: Exception) {
            coroutineContext.ensureActive()
            _state.value = UiState(generations = generations, message = "Unable to load this ancestor tree. Check your connection and try again.")
        }
        }
    }

    private fun radialLayout(generations: List<List<String>>): TreeLayout {
        val radiusStep = 190f
        val outerRadius = (generations.size - 1).coerceAtLeast(1) * radiusStep
        val canvas = outerRadius * 2 + 240f
        val center = canvas / 2
        val nodes = buildMap {
            generations.forEachIndexed { generation, handles ->
                if (generation == 0) put(handles.first(), PositionedNode(handles.first(), center - 90f, center - 40f))
                else handles.forEachIndexed { index, handle ->
                    val angle = (2.0 * Math.PI * index / handles.size) - Math.PI / 2
                    put(handle, PositionedNode(handle, center + kotlin.math.cos(angle).toFloat() * generation * radiusStep - 90f, center + kotlin.math.sin(angle).toFloat() * generation * radiusStep - 40f))
                }
            }
        }
        return TreeLayout(nodes, canvas, canvas)
    }

    data class UiState(
        val isLoading: Boolean = false,
        val generations: Int = 4,
        val mode: TreeMode = TreeMode.ANCESTORS,
        val rootHandle: String? = null,
        val graph: List<List<String>> = emptyList(),
        val edges: Set<app.grampsmaterial.feature_tree.TreeEdge> = emptySet(),
        val layout: TreeLayout? = null,
        val people: Map<String, GrampsPerson> = emptyMap(),
        val message: String? = null
    )
}
