package app.grampsmaterial.core_ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_database.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = SessionState(
                isLoading = false,
                isConnected = sessionManager.isConnectedFlow.first() &&
                    !sessionManager.getAccessToken().isNullOrBlank(),
                hasSelectedTree = sessionManager.selectedTreeIdFlow.first().isNotBlank()
            )
        }
    }

    data class SessionState(
        val isLoading: Boolean = true,
        val isConnected: Boolean = false,
        val hasSelectedTree: Boolean = false
    )
}
