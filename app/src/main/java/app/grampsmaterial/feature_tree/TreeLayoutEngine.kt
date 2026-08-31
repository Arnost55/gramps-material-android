package app.grampsmaterial.feature_tree

/** Immutable, screen-independent layout output for the tree renderer. */
data class PositionedNode(val handle: String, val x: Float, val y: Float)
data class TreeLayout(val nodes: Map<String, PositionedNode>, val width: Float, val height: Float)

class TreeLayoutEngine(
    private val columnGap: Float = 220f,
    private val rowGap: Float = 112f,
    private val nodeWidth: Float = 180f,
    private val nodeHeight: Float = 80f
) {
    fun layout(generations: List<List<String>>): TreeLayout {
        if (generations.isEmpty()) return TreeLayout(emptyMap(), nodeWidth, nodeHeight)
        val positions = buildMap {
            generations.forEachIndexed { generation, handles ->
                val count = handles.size.coerceAtLeast(1)
                handles.forEachIndexed { index, handle ->
                    put(handle, PositionedNode(handle, generation * columnGap, index * rowGap))
                }
            }
        }
        val widest = generations.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
        return TreeLayout(
            nodes = positions,
            width = nodeWidth + (generations.size - 1).coerceAtLeast(0) * columnGap,
            height = nodeHeight + (widest - 1) * rowGap
        )
    }
}
