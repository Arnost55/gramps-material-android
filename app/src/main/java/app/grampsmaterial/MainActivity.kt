package app.grampsmaterial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import app.grampsmaterial.core_ui.navigation.AppNavigator
import app.grampsmaterial.core_ui.theme.GrampsMaterialTheme
import app.grampsmaterial.feature_settings.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }
            val dynamicColor by themeViewModel.dynamicColor.collectAsState()
            val amoledMode by themeViewModel.amoledMode.collectAsState()
            val view = LocalView.current
            SideEffect {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            GrampsMaterialTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor,
                amoledMode = amoledMode
            ) {
                AppNavigator()
            }
        }
    }

}
