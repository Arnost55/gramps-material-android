package app.grampsmaterial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            val darkTheme by themeViewModel.darkTheme.collectAsState()
            val dynamicColor by themeViewModel.dynamicColor.collectAsState()
            val amoledMode by themeViewModel.amoledMode.collectAsState()
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
