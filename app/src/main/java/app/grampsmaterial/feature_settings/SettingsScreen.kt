package app.grampsmaterial.feature_settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.grampsmaterial.core_ui.theme.GrampsMaterialTheme
import app.grampsmaterial.feature_settings.viewmodel.SettingsViewModel
import app.grampsmaterial.core_database.AuthState
import app.grampsmaterial.core_network.NetworkState
import app.grampsmaterial.core_network.ServerReachability
import java.time.Duration
import java.time.Instant
import app.grampsmaterial.feature_settings.viewmodel.ThemeViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditServer: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val darkTheme by themeViewModel.darkTheme.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()
    val dynamicColor by themeViewModel.dynamicColor.collectAsState()
    val amoledMode by themeViewModel.amoledMode.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button and logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout"
                )
            }
        }

        // Server Section
        Text(
            text = "Server",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.serverUrl,
                onValueChange = { },
                label = { Text("Server URL") },
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onEditServer) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit server URL"
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        Text("Network: ${uiState.networkState.label()}", style = MaterialTheme.typography.bodyMedium)
        Text("Status: ${uiState.reachability.label()}", style = MaterialTheme.typography.bodyMedium)
        Text("Session: ${uiState.authState.label()}", style = MaterialTheme.typography.bodyMedium)
        if (uiState.treeName.isNotBlank()) Text("Tree: ${uiState.treeName}", style = MaterialTheme.typography.bodyMedium)
        Text("Last checked: ${uiState.reachability.lastCheckedLabel()}", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = settingsViewModel::retryServerCheck) { Text("Check server") }
        Spacer(modifier = Modifier.height(24.dp))
        
        // Appearance Section
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = if (themeMode == "system") "Theme: System" else "Dark Mode")
            if (themeMode == "system") {
                TextButton(onClick = { themeViewModel.setThemeMode("dark") }) { Text("Use dark") }
            } else {
                Switch(
                    checked = darkTheme,
                    onCheckedChange = { themeViewModel.setDarkTheme(it) }
                )
            }
        }
        if (themeMode != "system") {
            TextButton(onClick = { themeViewModel.setThemeMode("system") }) { Text("Use system theme") }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Dynamic Colors")
            Switch(
                checked = dynamicColor,
                onCheckedChange = { themeViewModel.setDynamicColor(it) }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "AMOLED Optimization")
            Switch(
                checked = amoledMode,
                onCheckedChange = { themeViewModel.setAmoledMode(it) }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        // Data Section
        Text(
            text = "Data",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { settingsViewModel.clearCache() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Clear Cached People and Trees")
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Cached people: ${uiState.cachedPeopleCount}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // About Section
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Version: ${uiState.appVersion}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun SettingsScreenPreviewWrapper() {
    GrampsMaterialTheme {
        SettingsScreen(onBack = {}, onEditServer = {}, onLogout = {})
    }
}


private fun NetworkState.label() = when (this) {
    NetworkState.Online -> "Online"
    NetworkState.Offline -> "Offline"
    NetworkState.Unknown -> "Unknown"
}

private fun AuthState.label() = when (this) {
    AuthState.SignedIn -> "Signed in"
    AuthState.SignedOut -> "Signed out"
    AuthState.SessionExpired -> "Session expired"
    AuthState.Unknown -> "Unknown"
}

private fun ServerReachability.label() = when (this) {
    is ServerReachability.Reachable -> "Server reachable"
    is ServerReachability.Unreachable -> "Server unavailable"
    ServerReachability.Checking -> "Checking server…"
    ServerReachability.Unknown -> "Not checked"
}

private fun ServerReachability.lastCheckedLabel(): String = when (this) {
    is ServerReachability.Reachable -> checkedAt.relativeLabel()
    is ServerReachability.Unreachable -> checkedAt.relativeLabel()
    else -> "Not yet"
}

private fun Instant.relativeLabel(): String = if (Duration.between(this, Instant.now()).seconds < 60) "Just now" else "Earlier"
