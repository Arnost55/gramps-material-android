package app.grampsmaterial.feature_tree

enum class TreeMode(val label: String) {
    ANCESTORS("Ancestors"),
    DESCENDANTS("Descendants"),
    FLEX("Flex")
}

data class RelationshipGraph(
    val generations: List<List<String>>,
    val edges: Set<TreeEdge>
)

/** Builds cycle-safe relationship views from cached parent/child links. */
class RelationshipGraphBuilder {
    fun build(
        root: String,
        mode: TreeMode,
        maxDepth: Int,
        parentsFor: (String) -> List<String>,
        childrenFor: (String) -> List<String>
    ): RelationshipGraph {
        val adjacent: (String) -> List<String> = when (mode) {
            TreeMode.ANCESTORS -> parentsFor
            TreeMode.DESCENDANTS -> childrenFor
            TreeMode.FLEX -> { handle -> (parentsFor(handle) + childrenFor(handle)).distinct() }
        }
        val seen = mutableSetOf(root)
        val levels = mutableListOf(listOf(root))
        val edges = linkedSetOf<TreeEdge>()
        var frontier = levels.first()

        var depth = 1
        while (depth < maxDepth && frontier.isNotEmpty()) {
            val next = mutableListOf<String>()
            frontier.forEach { handle ->
                adjacent(handle).forEach { related ->
                    if (seen.add(related)) next += related
                    when {
                        related in parentsFor(handle) -> edges += TreeEdge(handle, related)
                        handle in parentsFor(related) -> edges += TreeEdge(related, handle)
                    }
                }
            }
            if (next.isEmpty()) break
            levels += next
            frontier = next
            depth++
        }
        return RelationshipGraph(levels, edges)
    }
}
