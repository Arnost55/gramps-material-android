package app.grampsmaterial.feature_places

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.grampsmaterial.core_network.PlaceRepository
import app.grampsmaterial.core_network.models.GrampsPlace
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun PlacesScreen(viewModel: PlacesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Places", style = MaterialTheme.typography.headlineSmall) }
        when {
            state.loading -> item { CircularProgressIndicator(Modifier.padding(24.dp)) }
            state.error != null -> item { Text(state.error!!, color = MaterialTheme.colorScheme.error) }
            state.places.isEmpty() -> item { Text("No places in this tree.", Modifier.padding(top = 16.dp)) }
            else -> items(state.places) { place ->
                Card(Modifier.fillMaxWidth().padding(top = 10.dp).clickable(enabled = place.lat != null && place.long != null) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${place.lat},${place.long}?q=${Uri.encode(place.title.orEmpty())}")))
                }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(place.title ?: "Unnamed place", style = MaterialTheme.typography.titleMedium)
                        if (place.lat != null && place.long != null) Text("${place.lat}, ${place.long}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else Text("No map coordinates", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@HiltViewModel
class PlacesViewModel @Inject constructor(private val repository: PlaceRepository) : ViewModel() {
    data class State(val loading: Boolean = true, val places: List<GrampsPlace> = emptyList(), val error: String? = null)
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()
    init { viewModelScope.launch { _state.value = runCatching { State(loading = false, places = repository.getAllPlaces()) }.getOrElse { State(loading = false, error = "Unable to load places.") } } }
}
