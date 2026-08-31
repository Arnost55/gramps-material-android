package app.grampsmaterial.feature_settings.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.BuildConfig
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.PersonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

private const val TAG = "SettingsViewModel"

@HiltViewModel
open class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val personRepository: PersonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    open val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch {
            try {
                val serverUrl = sessionManager.serverUrlFlow.first()
                val isConnected = sessionManager.isConnectedFlow.first()
                val themeMode = sessionManager.themeModeFlow.first()
                val isDarkMode = when (themeMode) {
                    "dark" -> true
                    else -> false
                }
                val useDynamicColors = sessionManager.dynamicColorsFlow.first()
                val useAmoledOptimization = sessionManager.amoledModeFlow.first()
                
                _uiState.update { it.copy(
                    serverUrl = serverUrl,
                    isConnected = isConnected,
                    isDarkMode = isDarkMode,
                    useDynamicColors = useDynamicColors,
                    useAmoledOptimization = useAmoledOptimization,
                    cachedPeopleCount = personRepository.getCachedPersonCount(),
                    appVersion = BuildConfig.VERSION_NAME
                ) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading settings state", e)
            }
        }
    }

    open fun logout() {
        viewModelScope.launch {
            try {
                personRepository.clearCache()
                sessionManager.logout()
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout", e)
            }
        }
    }

    open fun clearCache() {
        viewModelScope.launch {
            try {
                personRepository.clearCache()
                loadState()
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing cache", e)
            }
        }
    }

    open fun refresh() {        loadState()
    }

    data class UiState(
        val serverUrl: String = "",
        val isConnected: Boolean = false,
        val isDarkMode: Boolean = false,
        val useDynamicColors: Boolean = false,
        val useAmoledOptimization: Boolean = false,
        val cachedPeopleCount: Int = 0,
        val appVersion: String = "0.1.0"
    )
}
