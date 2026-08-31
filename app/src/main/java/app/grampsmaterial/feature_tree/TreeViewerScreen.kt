package app.grampsmaterial.feature_tree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.grampsmaterial.core_network.models.displayName
import app.grampsmaterial.core_network.models.lifeYears
import app.grampsmaterial.feature_tree.viewmodel.TreeViewerViewModel

@Composable
fun TreeViewerScreen(
    onBack: () -> Unit,
    onPersonSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TreeViewerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var generations by remember { mutableIntStateOf(4) }
    var mode by remember { mutableStateOf(TreeMode.ANCESTORS) }
    var scale by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    LaunchedEffect(generations, mode) { viewModel.load(generations, mode) }
    LaunchedEffect(state.layout, canvasSize) {
        state.layout?.takeIf { canvasSize != IntSize.Zero }?.let { layout ->
            scale = minOf(1f, (canvasSize.width - 32f) / layout.width, (canvasSize.height - 32f) / layout.height)
            pan = Offset(
                (canvasSize.width - layout.width * scale) / 2f,
                (canvasSize.height - layout.height * scale) / 2f
            )
        }
    }

    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${mode.label} tree", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { generations = (generations - 1).coerceAtLeast(2) }) { Icon(Icons.Outlined.Remove, "Fewer generations") }
                Text("$generations generations")
                IconButton(onClick = { generations = (generations + 1).coerceAtMost(6) }) { Icon(Icons.Outlined.Add, "More generations") }
                IconButton(onClick = { scale = 1f; pan = Offset.Zero; viewModel.load(generations, mode) }) { Icon(Icons.Outlined.Refresh, "Fit and refresh") }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TreeMode.entries.forEach { option ->
                FilterChip(
                    selected = mode == option,
                    onClick = { mode = option },
                    label = { Text(option.label) }
                )
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.message != null -> Text(requireNotNull(state.message), Modifier.padding(32.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.layout != null -> {
                    val layout = requireNotNull(state.layout)
                    val primary = MaterialTheme.colorScheme.primary
                    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
                    val onPrimary = MaterialTheme.colorScheme.onPrimary
                    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
                    Canvas(
                        Modifier.fillMaxSize()
                            .onSizeChanged { canvasSize = it }
                            .pointerInput(Unit) { detectTransformGestures { _, delta, zoom, _ -> scale = (scale * zoom).coerceIn(.45f, 2.5f); pan += delta } }
                            .pointerInput(layout, scale, pan) { detectTapGestures { point ->
                                val content = (point - pan) / scale
                                layout.nodes.values.firstOrNull { node -> content.x in node.x..node.x + 180f && content.y in node.y..node.y + 80f }?.let { onPersonSelected(it.handle) }
                            } }
                    ) {
                        withTransform({ translate(pan.x, pan.y); scale(scale, scale) }) {
                            state.edges.forEach { edge ->
                                val child = layout.nodes[edge.childHandle] ?: return@forEach
                                val parent = layout.nodes[edge.parentHandle] ?: return@forEach
                                drawLine(primary.copy(alpha = .42f), Offset(child.x + 180f, child.y + 40f), Offset(parent.x, parent.y + 40f), strokeWidth = 3f)
                            }
                            layout.nodes.values.forEach { node ->
                                val isRoot = node.handle == state.rootHandle
                                drawRoundRect(if (isRoot) primary else surfaceVariant, Offset(node.x, node.y), Size(180f, 80f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f))
                                val person = state.people[node.handle]
                                drawContext.canvas.nativeCanvas.apply {
                                    drawText(person?.displayName()?.take(20) ?: node.handle.take(12), node.x + 12f, node.y + 33f, android.graphics.Paint().apply { color = if (isRoot) onPrimary.toArgb() else onSurfaceVariant.toArgb(); textSize = 17f; isAntiAlias = true })
                                    drawText(person?.lifeYears().orEmpty().take(18), node.x + 12f, node.y + 60f, android.graphics.Paint().apply { color = if (isRoot) onPrimary.copy(alpha = .78f).toArgb() else onSurfaceVariant.copy(alpha = .72f).toArgb(); textSize = 13f; isAntiAlias = true })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
