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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.grampsmaterial.core_network.models.displayName
import app.grampsmaterial.core_network.models.lifeYears
import app.grampsmaterial.feature_person.viewmodel.PersonProfileViewModel
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest

@Composable
fun PersonProfileScreen(
    personHandle: String,
    onBack: () -> Unit,
    onPersonSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var editFirstName by remember { mutableStateOf("") }
    var editSurname by remember { mutableStateOf("") }
    LaunchedEffect(personHandle) { viewModel.loadPerson(personHandle) }
    LaunchedEffect(state.person?.handle) {
        state.person?.let { person ->
            editFirstName = person.primary_name?.first_name.orEmpty()
            editSurname = person.primary_name?.surname_list?.firstOrNull()?.surname.orEmpty()
        }
    }
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
                Section("Edit person") {
                    OutlinedTextField(editFirstName, { editFirstName = it }, label = { Text("First name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(editSurname, { editSurname = it }, label = { Text("Surname") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { viewModel.updateName(editFirstName, editSurname) }, enabled = !state.isSaving) {
                        Text(if (state.isSaving) "Saving…" else "Save name")
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
                Section("DNA matches") {
                    if (state.dnaMatches.isEmpty()) Text("No DNA matches were returned for this person.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else state.dnaMatches.forEach { match ->
                        Text(match.relation ?: "DNA match", style = MaterialTheme.typography.bodyLarge)
                        if (match.ancestor_handles.isNotEmpty()) Text("${match.ancestor_handles.size} common ancestor record(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Section("Media") {
                    if (state.media.isEmpty()) Text("No media was returned for this person.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else {
                        state.media.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { media ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(media.thumbnailUrl)
                                            .httpHeaders(
                                                NetworkHeaders.Builder()
                                                    .set("Authorization", "Bearer ${media.accessToken}")
                                                    .build()
                                            )
                                            .build(),
                                        contentDescription = "Media item",
                                        modifier = Modifier.size(96.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        if (person.media_list.size > state.media.size) Text("Showing the first ${state.media.size} items.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Section("Sources, citations & notes") {
                    if (state.research.isEmpty()) Text("No research records were returned for this person.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else state.research.forEach { item ->
                        Text(item.kind, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(item.title, style = MaterialTheme.typography.bodyLarge)
                        item.detail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
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
