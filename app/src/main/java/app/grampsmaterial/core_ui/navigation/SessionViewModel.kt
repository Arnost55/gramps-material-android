package app.grampsmaterial.core_ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_database.AuthState
import app.grampsmaterial.core_network.AuthRepository
import app.grampsmaterial.core_network.NetworkState
import app.grampsmaterial.core_network.NetworkStateMonitor
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
    private val peopleCacheScheduler: PeopleCacheScheduler,
    private val authRepository: AuthRepository,
    private val networkStateMonitor: NetworkStateMonitor
) : ViewModel() {
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                sessionManager.authStateFlow,
                sessionManager.serverUrlFlow,
                sessionManager.allowInsecureHttpFlow,
                networkStateMonitor.state
            ) { authState, url, insecure, networkState -> arrayOf(authState, url, insecure, networkState) }
                .collect { (authState, url, insecure, networkState) ->
                    if (authState as AuthState == AuthState.SignedIn && (url as String).isNotBlank() && networkState == NetworkState.Online) {
                        authRepository.verifyConnection(url as String, insecure as Boolean)
                    }
                }
        }
        viewModelScope.launch {
            combine(
                sessionManager.authStateFlow,
                sessionManager.selectedTreeIdFlow
            ) { authState, selectedTreeId ->
                SessionState(
                    isLoading = false,
                    isAuthenticated = authState == AuthState.SignedIn && !sessionManager.getAccessToken().isNullOrBlank(),
                    hasSelectedTree = selectedTreeId.isNotBlank()
                )
            }.collect { state ->
                _state.value = state
                if (state.isAuthenticated) peopleCacheScheduler.enqueueRefresh()
            }
        }
    }

    data class SessionState(
        val isLoading: Boolean = true,
        val isAuthenticated: Boolean = false,
        val hasSelectedTree: Boolean = false
    )
}
