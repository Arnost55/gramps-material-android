package app.grampsmaterial.feature_person

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.grampsmaterial.core_network.models.displayName
import app.grampsmaterial.core_network.models.lifeYears
import app.grampsmaterial.feature_person.viewmodel.PersonProfileViewModel

@Composable
fun PersonProfileScreen(
    personHandle: String,
    onBack: () -> Unit,
    onPersonSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(personHandle) { viewModel.loadPerson(personHandle) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
            Text("Person", style = MaterialTheme.typography.titleLarge)
        }
        when {
            state.isLoading && state.person == null -> androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Text(requireNotNull(state.error), color = MaterialTheme.colorScheme.error)
            state.person != null -> {
                val person = requireNotNull(state.person)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.layout.Box(Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                        Card { Text(person.displayName().take(1), Modifier.padding(24.dp), fontSize = 26.sp, fontWeight = FontWeight.Bold) }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(person.displayName(), style = MaterialTheme.typography.headlineSmall)
                        person.lifeYears()?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        if (state.isHomePerson) {
                            Text("Home person", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Button(onClick = viewModel::setAsHomePerson) { Text("Set as home person") }
                        }
                        Button(onClick = viewModel::toggleBookmark) {
                            Text(if (state.isBookmarked) "Remove bookmark" else "Bookmark person")
                        }
                        state.relationshipToHome?.let { relationship ->
                            Text("Home person: $relationship", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                        state.notice?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error) }
                        if (state.isStale) Text("Offline — showing cached data", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
                RelationshipSection("Parents", state.relationships.parents, onPersonSelected)
                RelationshipSection("Partners", state.relationships.partners, onPersonSelected)
                RelationshipSection("Children", state.relationships.children, onPersonSelected)
                Section("Events") {
                    val events = listOfNotNull(person.profile?.birth, person.profile?.death) + person.profile?.events.orEmpty()
                    if (events.isEmpty()) Text("No events were returned for this person.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else events.forEach { event -> Text(listOfNotNull(event.type, event.date, event.place_name ?: event.place).joinToString(" · ")) }
                }
                Section("Timeline") {
                    if (state.timeline.isEmpty()) Text("No timeline events were returned for this person.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else state.timeline.forEach { event ->
                        Text(listOfNotNull(event.date, event.type, event.description, event.place_name ?: event.place).joinToString(" · "))
                    }
                }
                Section("Media") { Text(if (person.media_list.isEmpty()) "No media was returned for this person." else "${person.media_list.size} media item(s) available.") }
                Section("Sources & citations") { Text(if (person.citation_list.isEmpty()) "No citations were returned for this person." else "${person.citation_list.size} citation(s) available.") }
                Section("Notes") { Text(if (person.note_list.isEmpty()) "No notes were returned for this person." else "${person.note_list.size} note(s) available.") }
            }
        }
    }
}

@Composable
private fun RelationshipSection(
    title: String,
    people: List<PersonProfileViewModel.RelatedPerson>,
    onPersonSelected: (String) -> Unit
) = Section(title) {
    if (people.isEmpty()) Text("No $title were returned for this person.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    else people.forEach { person ->
        Row(Modifier.fillMaxWidth().clickable { onPersonSelected(person.handle) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(person.displayName, style = MaterialTheme.typography.bodyLarge)
                person.lifeYears?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Icon(Icons.Outlined.ChevronRight, "Open ${person.displayName}")
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); content() } }
}
