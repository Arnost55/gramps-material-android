package app.grampsmaterial.feature_tree.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_network.TreeRepository
import app.grampsmaterial.core_network.models.GrampsTree
import app.grampsmaterial.core_database.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

private const val TAG = "TreeSelectionViewModel"

@HiltViewModel
class TreeSelectionViewModel @Inject constructor(
    private val treeRepository: TreeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _navigateToHome = MutableStateFlow(false)
    val navigateToHome: StateFlow<Boolean> = _navigateToHome.asStateFlow()
    
    val serverUrl: StateFlow<String> = sessionManager.serverUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val selectedTreeId: StateFlow<String> = sessionManager.selectedTreeIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val selectedTreeName: StateFlow<String> = sessionManager.selectedTreeNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun loadTrees() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val trees = treeRepository.cachedTrees()
                _uiState.update { it.copy(
                    isLoading = false,
                    trees = trees
                ) }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error loading trees", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load trees from server"
                ) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading trees", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load trees: ${e.localizedMessage}"
                ) }
            }
        }
    }

    fun selectTree(tree: GrampsTree) {
        viewModelScope.launch {
            try {
                treeRepository.saveSelectedTree(tree.id, tree.name)
                _navigateToHome.update { true }
            } catch (e: Exception) {
                Log.e(TAG, "Error selecting tree", e)
            }
        }
    }

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val trees: List<GrampsTree> = emptyList()
    )
}
