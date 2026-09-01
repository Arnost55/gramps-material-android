package app.grampsmaterial.feature_home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.grampsmaterial.feature_home.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onTreeChange: () -> Unit,
    onSearchClick: () -> Unit,
    onPersonSelected: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var customizing by remember { mutableStateOf(false) }
    val monthBirthdays = uiState.birthdays.filter { it.month == uiState.birthdayMonth }
    val monthName = java.time.Month.of(uiState.birthdayMonth).name.lowercase().replaceFirstChar(Char::titlecase)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Home", style = MaterialTheme.typography.headlineMedium)
                if (uiState.username.isNotBlank()) {
                    Text(
                        "Welcome back, ${uiState.username}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = { customizing = !customizing }) { Text("Customize") }
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onSettingsClick)
            )
        }

        if (customizing) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Dashboard widgets", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("tree" to "Tree", "home" to "Home person", "search" to "Search", "stats" to "Stats", "birthdays" to "Birthdays", "recent" to "Recent").forEach { (id, label) ->
                            FilterChip(selected = id in uiState.widgets, onClick = { viewModel.toggleWidget(id) }, label = { Text(label) })
                        }
                    }
                    FilterChip(selected = uiState.compact, onClick = { viewModel.setCompact(!uiState.compact) }, label = { Text("Compact cards") })
                }
            }
        }

        if ("tree" in uiState.widgets) ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountTree,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Current family tree", style = MaterialTheme.typography.labelLarge)
                    Text(
                        uiState.selectedTreeName.ifBlank { "Choose a tree to begin" },
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                AssistChip(
                    onClick = onTreeChange,
                    label = { Text("Change") }
                )
            }
        }

        if (uiState.isUsingCachedData) {
            Text(
                "Using cached data — connect to sync the latest changes.",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if ("home" in uiState.widgets) uiState.homePerson?.let { person ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().clickable { onPersonSelected(person.handle) },
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Home person", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(person.displayName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    person.lifeYears?.let { Text(it, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                    Text("Opens your profile and anchors the ancestor tree", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        if ("search" in uiState.widgets) ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSearchClick)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null)
                Spacer(Modifier.size(16.dp))
                Column {
                    Text("Search your family tree", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Find people, places, and events",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if ("stats" in uiState.widgets) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "People loaded",
                value = uiState.personCount.toString(),
                icon = Icons.Outlined.People
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Birthdays in $monthName",
                value = monthBirthdays.size.toString(),
                icon = Icons.Outlined.AccountTree
            )
        }

        if ("birthdays" in uiState.widgets) ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Birthdays", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.setBirthdayMonth(if (uiState.birthdayMonth == 1) 12 else uiState.birthdayMonth - 1) }) { Text("‹") }
                    Text(monthName, style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { viewModel.setBirthdayMonth(if (uiState.birthdayMonth == 12) 1 else uiState.birthdayMonth + 1) }) { Text("›") }
                }
                if (monthBirthdays.isEmpty()) {
                    Text("No known birthdays in $monthName.", color = MaterialTheme.colorScheme.onTertiaryContainer)
                } else {
                    monthBirthdays.forEach { birthday ->
                        Text("${birthday.displayName} · ${birthday.date}", color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
        }

        Text("Recently viewed", style = MaterialTheme.typography.titleLarge)
        if (uiState.recentPeople.isEmpty()) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "People you open will appear here.",
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    uiState.recentPeople.forEach { person ->
                        Text(
                            person.displayName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPersonSelected(person.handle) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
