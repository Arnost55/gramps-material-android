package app.grampsmaterial.feature_settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_database.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
open class ThemeViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _darkTheme = MutableStateFlow(false)
    open val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    private val _dynamicColor = MutableStateFlow(true)
    open val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _amoledMode = MutableStateFlow(false)
    open val amoledMode: StateFlow<Boolean> = _amoledMode.asStateFlow()

    init {
        viewModelScope.launch { sessionManager.themeModeFlow.collect { _darkTheme.value = it == "dark" } }
        viewModelScope.launch { sessionManager.dynamicColorsFlow.collect { _dynamicColor.value = it } }
        viewModelScope.launch { sessionManager.amoledModeFlow.collect { _amoledMode.value = it } }
    }

    private fun loadState() {
        viewModelScope.launch {
            try {
                val themeMode = sessionManager.themeModeFlow.first()
                val isDark = when (themeMode) {
                    "dark" -> true
                    else -> false
                }
                
                _darkTheme.update { isDark }
                _dynamicColor.update { sessionManager.dynamicColorsFlow.first() }
                _amoledMode.update { sessionManager.amoledModeFlow.first() }
            } catch (e: Exception) {
                // Keep defaults
            }
        }
    }

    open fun setDarkTheme(darkTheme: Boolean) {
        viewModelScope.launch {
            val mode = if (darkTheme) "dark" else "light"
            sessionManager.setThemeMode(mode)
            _darkTheme.update { darkTheme }
        }
    }

    open fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.setDynamicColors(enabled)
            _dynamicColor.update { enabled }
        }
    }

    open fun setAmoledMode(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.setAmoledMode(enabled)
            _amoledMode.update { enabled }
        }
    }

    open fun refresh() {
        loadState()
    }
}
