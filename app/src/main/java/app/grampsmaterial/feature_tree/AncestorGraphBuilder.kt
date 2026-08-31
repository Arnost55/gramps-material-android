package app.grampsmaterial.feature_tree

/** Pure, cycle-safe ancestor traversal. Network and Compose code stay outside this builder. */
data class AncestorGraph(
    val rootHandle: String,
    val generations: List<List<String>>
)

class AncestorGraphBuilder {
    /**
     * Builds root-first generations. A handle appears once even when pedigree collapse or a
     * malformed family cycle is present.
     */
    fun build(
        rootHandle: String,
        maxGenerations: Int,
        parentsFor: (String) -> List<String>
    ): AncestorGraph {
        require(maxGenerations in 1..6) { "maxGenerations must be between 1 and 6" }
        val seen = mutableSetOf(rootHandle)
        val generations = mutableListOf(listOf(rootHandle))
        var frontier = generations.first()

        repeat(maxGenerations - 1) {
            val next = frontier.flatMap(parentsFor).filter(seen::add)
            if (next.isEmpty()) return@repeat
            generations += next
            frontier = next
        }
        return AncestorGraph(rootHandle, generations)
    }
}
