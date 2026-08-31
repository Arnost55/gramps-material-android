package app.grampsmaterial.feature_tree

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RelationshipGraphBuilderTest {
    private val builder = RelationshipGraphBuilder()

    @Test fun `descendant mode follows children`() {
        val graph = builder.build(
            root = "P1", mode = TreeMode.DESCENDANTS, maxDepth = 3,
            parentsFor = { emptyList() },
            childrenFor = { if (it == "P1") listOf("P2", "P3") else emptyList() }
        )
        assertEquals(listOf(listOf("P1"), listOf("P2", "P3")), graph.generations)
    }

    @Test fun `flex mode traverses both directions without cycles`() {
        val parents = mapOf("P2" to listOf("P1"), "P3" to listOf("P2"), "P1" to listOf("P3"))
        val children = mapOf("P1" to listOf("P2"), "P2" to listOf("P3"), "P3" to listOf("P1"))
        val graph = builder.build("P1", TreeMode.FLEX, Int.MAX_VALUE, { parents[it].orEmpty() }, { children[it].orEmpty() })
        assertEquals(3, graph.generations.flatten().distinct().size)
        assertTrue(graph.generations.flatten().containsAll(listOf("P1", "P2", "P3")))
    }
}
