package app.grampsmaterial.feature_auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.AuthRepository
import app.grampsmaterial.core_network.GrampsServer
import app.grampsmaterial.core_network.ServerReadiness
import app.grampsmaterial.core_sync.PeopleCacheScheduler
import app.grampsmaterial.core_network.models.TokenRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val peopleCacheScheduler: PeopleCacheScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    fun testConnection(serverUrl: String, allowInsecureHttp: Boolean) {
        viewModelScope.launch {
            val normalized = validateUrl(serverUrl, allowInsecureHttp) ?: return@launch
            _uiState.update { it.copy(isLoading = true, errorMessage = null, connectionMessage = null) }
            _uiState.update {
                when (authRepository.verifyConnection(normalized, allowInsecureHttp)) {
                    ServerReadiness.Ready -> it.copy(isLoading = false, connectionMessage = "Server is ready")
                    ServerReadiness.Unreachable -> it.copy(isLoading = false, errorMessage = "Cannot reach this server. Check the address and your connection.")
                    ServerReadiness.TlsFailure -> it.copy(isLoading = false, errorMessage = "The secure connection could not be verified.")
                    is ServerReadiness.Unsupported -> it.copy(isLoading = false, errorMessage = "This server is not a compatible Gramps Web server.")
                }
            }
        }
    }

    fun connectToServer(serverUrl: String, username: String, password: String, allowInsecureHttp: Boolean) {
        viewModelScope.launch {
            val normalized = validateUrl(serverUrl, allowInsecureHttp) ?: return@launch
            if (username.isBlank() || password.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Enter both your username and password.") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, errorMessage = null, connectionMessage = null) }
            try {
                when (authRepository.verifyConnection(normalized, allowInsecureHttp)) {
                    ServerReadiness.Ready -> Unit
                    ServerReadiness.Unreachable -> error("Cannot reach this server. Check the address and your connection.")
                    ServerReadiness.TlsFailure -> error("The secure connection could not be verified.")
                    is ServerReadiness.Unsupported -> error("This server is not a compatible Gramps Web server.")
                }
                val token = authRepository.login(normalized, TokenRequest(username, password), allowInsecureHttp)
                sessionManager.setAllowInsecureHttp(allowInsecureHttp)
                sessionManager.saveServerUrl(normalized)
                sessionManager.saveUsername(username.trim())
                authRepository.saveTokens(token.access_token, token.refresh_token)
                peopleCacheScheduler.enqueueRefresh()
                _uiState.update { it.copy(isLoading = false) }
                _authSuccess.value = true
            } catch (_: HttpException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Incorrect username or password, or the server rejected the sign-in.") }
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Unable to sign in right now.") }
            }
        }
    }

    private fun validateUrl(serverUrl: String, allowInsecureHttp: Boolean): String? = try {
        GrampsServer.normalizeUrl(serverUrl).also { normalized ->
            require(allowInsecureHttp || !GrampsServer.isInsecure(normalized)) {
                "HTTPS is required unless you enable insecure local server."
            }
        }
    } catch (error: IllegalArgumentException) {
        _uiState.update { it.copy(errorMessage = error.message) }
        null
    }

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val connectionMessage: String? = null
    )
}
