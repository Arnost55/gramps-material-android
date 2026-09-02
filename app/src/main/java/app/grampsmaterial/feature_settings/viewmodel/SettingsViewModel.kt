package app.grampsmaterial.feature_settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.BuildConfig
import app.grampsmaterial.core_database.AuthState
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.AuthRepository
import app.grampsmaterial.core_network.NetworkState
import app.grampsmaterial.core_network.NetworkStateMonitor
import app.grampsmaterial.core_network.PersonRepository
import app.grampsmaterial.core_network.ServerReachability
import app.grampsmaterial.core_network.ServerReachabilityTracker
import app.grampsmaterial.core_network.ServerReadiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val personRepository: PersonRepository,
    private val authRepository: AuthRepository,
    private val networkStateMonitor: NetworkStateMonitor,
    private val reachabilityTracker: ServerReachabilityTracker
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                sessionManager.serverUrlFlow,
                sessionManager.selectedTreeNameFlow,
                sessionManager.authStateFlow,
                networkStateMonitor.state,
                reachabilityTracker.state
            ) { serverUrl, treeName, authState, networkState, reachability ->
                UiState(
                    serverUrl = serverUrl,
                    treeName = treeName,
                    authState = authState,
                    networkState = networkState,
                    reachability = reachability,
                    appVersion = BuildConfig.VERSION_NAME
                )
            }.collect { state ->
                _uiState.value = state.copy(cachedPeopleCount = personRepository.getCachedPersonCount())
            }
        }
    }

    fun retryServerCheck() = viewModelScope.launch {
        val url = sessionManager.serverUrlFlow.first()
        if (url.isBlank() || networkStateMonitor.state.value == NetworkState.Offline) return@launch
        reachabilityTracker.checking()
        when (authRepository.verifyConnection(url, sessionManager.allowInsecureHttpFlow.first())) {
            ServerReadiness.Ready -> reachabilityTracker.reachable()
            ServerReadiness.Unreachable -> Unit
            ServerReadiness.TlsFailure -> Unit
            is ServerReadiness.Unsupported -> Unit
        }
    }

    fun logout() = viewModelScope.launch {
        personRepository.clearCache()
        sessionManager.logout()
    }

    fun clearCache() = viewModelScope.launch {
        personRepository.clearCache()
        _uiState.update { it.copy(cachedPeopleCount = 0) }
    }

    data class UiState(
        val serverUrl: String = "",
        val treeName: String = "",
        val authState: AuthState = AuthState.Unknown,
        val networkState: NetworkState = NetworkState.Unknown,
        val reachability: ServerReachability = ServerReachability.Unknown,
        val cachedPeopleCount: Int = 0,
        val appVersion: String = ""
    )
}
