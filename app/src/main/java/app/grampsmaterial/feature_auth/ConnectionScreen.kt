package app.grampsmaterial.feature_auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.grampsmaterial.core_network.GrampsServer
import app.grampsmaterial.feature_auth.viewmodel.AuthViewModel

@Composable
fun ConnectionScreen(
    onConnected: (GrampsServer) -> Unit,
    onBackToWelcome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()
    val authenticated by viewModel.authSuccess.collectAsState()

    LaunchedEffect(authenticated) {
        if (authenticated) onConnected(GrampsServer(serverUrl))
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Icon(Icons.Outlined.AccountTree, contentDescription = null, modifier = Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary)
        Text("Connect to Gramps Web", style = MaterialTheme.typography.headlineSmall)
        Text("Sign in to the Gramps Web server that keeps your family history.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(serverUrl, { serverUrl = it }, Modifier.fillMaxWidth(), label = { Text("Server URL") }, placeholder = { Text("https://family.example.com") }, singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Uri))
        OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), label = { Text("Username") }, singleLine = true)
        OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password))
        state.errorMessage?.let { StatusCard(it, true) }
        state.connectionMessage?.let { StatusCard(it, false) }
        OutlinedButton(
            onClick = { viewModel.testConnection(serverUrl, false) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) { Text("Test connection") }
        Button(
            onClick = { viewModel.connectToServer(serverUrl, username, password, false) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text("Sign in")
        }
        OutlinedButton(onClick = onBackToWelcome, modifier = Modifier.fillMaxWidth(), enabled = !state.isLoading) { Text("Back") }
    }
}

@Composable
private fun StatusCard(message: String, isError: Boolean) {
    ElevatedCard(colors = androidx.compose.material3.CardDefaults.elevatedCardColors(containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer)) {
        Text(message, Modifier.padding(16.dp), color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer)
    }
}
