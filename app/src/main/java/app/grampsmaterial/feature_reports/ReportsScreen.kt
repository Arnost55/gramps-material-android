package app.grampsmaterial.feature_reports

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.grampsmaterial.core_network.ReportRepository
import app.grampsmaterial.core_network.models.GrampsReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Reports", style = MaterialTheme.typography.headlineSmall) }
        when {
            state.loading -> item { CircularProgressIndicator(Modifier.padding(24.dp)) }
            state.error != null -> item { Text(state.error!!, color = MaterialTheme.colorScheme.error) }
            state.reports.isEmpty() -> item { Text("No reports are available on this server.", Modifier.padding(top = 16.dp)) }
            else -> items(state.reports, key = { it.id }) { report ->
                Card(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(report.name ?: report.id, style = MaterialTheme.typography.titleMedium)
                        report.description?.takeIf(String::isNotBlank)?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}

@HiltViewModel
class ReportsViewModel @Inject constructor(private val repository: ReportRepository) : ViewModel() {
    data class State(val loading: Boolean = true, val reports: List<GrampsReport> = emptyList(), val error: String? = null)
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()
    init { viewModelScope.launch { _state.value = runCatching { State(loading = false, reports = repository.getReports()) }.getOrElse { State(loading = false, error = "Unable to load server reports.") } } }
}
