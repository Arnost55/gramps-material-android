package app.grampsmaterial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.grampsmaterial.ui.theme.GrampsMaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GrampsMaterialTheme {
                GrampsMaterialApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrampsMaterialApp() {
    var serverUrl by remember { mutableStateOf("") }
    var connected by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gramps Material", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Unofficial Gramps Web client",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (connected) {
                NavigationBar {
                    listOf(
                        Triple("Home", Icons.Outlined.Home, 0),
                        Triple("Search", Icons.Outlined.Search, 1),
                        Triple("Tree", Icons.Outlined.AccountTree, 2)
                    ).forEach { (label, icon, index) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (!connected) {
            ConnectScreen(
                padding = padding,
                serverUrl = serverUrl,
                onServerUrlChange = { serverUrl = it },
                onConnect = { connected = serverUrl.trim().startsWith("https://") }
            )
        } else {
            HomeShell(
                padding = padding,
                selectedTab = selectedTab,
                serverUrl = serverUrl
            )
        }
    }
}

@Composable
private fun ConnectScreen(
    padding: PaddingValues,
    serverUrl: String,
    onServerUrlChange: (String) -> Unit,
    onConnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Connect your family tree", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Point the app at your self-hosted Gramps Web server. Authentication comes next.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = serverUrl,
            onValueChange = onServerUrlChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Gramps Web URL") },
            placeholder = { Text("https://family.example.com") }
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onConnect,
            modifier = Modifier.fillMaxWidth(),
            enabled = serverUrl.trim().startsWith("https://")
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun HomeShell(
    padding: PaddingValues,
    selectedTab: Int,
    serverUrl: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (selectedTab) {
            0 -> {
                Text("Your family", style = MaterialTheme.typography.headlineMedium)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Server", style = MaterialTheme.typography.labelLarge)
                        Text(serverUrl, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("MVP")
                            Text("•")
                            Text("API integration next")
                        }
                    }
                }
            }
            1 -> {
                Text("Search", style = MaterialTheme.typography.headlineMedium)
                Text("Person and family search will live here.")
            }
            else -> {
                Text("Family tree", style = MaterialTheme.typography.headlineMedium)
                Text("Pinch-to-zoom interactive tree canvas is the flagship feature.")
            }
        }
    }
}
