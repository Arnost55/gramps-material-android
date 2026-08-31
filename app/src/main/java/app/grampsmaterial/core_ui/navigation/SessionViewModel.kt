package app.grampsmaterial.core_ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_sync.PeopleCacheScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val peopleCacheScheduler: PeopleCacheScheduler
) : ViewModel() {
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                sessionManager.isConnectedFlow,
                sessionManager.selectedTreeIdFlow
            ) { isConnected, selectedTreeId ->
                SessionState(
                    isLoading = false,
                    isConnected = isConnected && !sessionManager.getAccessToken().isNullOrBlank(),
                    hasSelectedTree = selectedTreeId.isNotBlank()
                )
            }.collect { state ->
                _state.value = state
                if (state.isConnected) peopleCacheScheduler.enqueueRefresh()
            }
        }
    }

    data class SessionState(
        val isLoading: Boolean = true,
        val isConnected: Boolean = false,
        val hasSelectedTree: Boolean = false
    )
}
