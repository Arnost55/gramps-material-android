package app.grampsmaterial.feature_tree

import org.junit.Assert.assertEquals
import org.junit.Test

class AncestorGraphBuilderTest {
    private val builder = AncestorGraphBuilder()

    @Test
    fun buildsRootFirstGenerations() {
        val graph = builder.build("root", 4) { handle ->
            mapOf("root" to listOf("mother", "father"), "mother" to listOf("gm"), "father" to listOf("gf")).getOrDefault(handle, emptyList())
        }
        assertEquals(listOf(listOf("root"), listOf("mother", "father"), listOf("gm", "gf")), graph.generations)
        assertEquals(
            setOf(
                TreeEdge("root", "mother"), TreeEdge("root", "father"),
                TreeEdge("mother", "gm"), TreeEdge("father", "gf")
            ),
            graph.edges
        )
    }

    @Test
    fun stopsCyclesAndPedigreeCollapse() {
        val graph = builder.build("root", 6) { handle ->
            mapOf("root" to listOf("a", "b"), "a" to listOf("shared"), "b" to listOf("shared"), "shared" to listOf("root")).getOrDefault(handle, emptyList())
        }
        assertEquals(listOf(listOf("root"), listOf("a", "b"), listOf("shared")), graph.generations)
        assertEquals(
            setOf(
                TreeEdge("root", "a"), TreeEdge("root", "b"),
                TreeEdge("a", "shared"), TreeEdge("b", "shared")
            ),
            graph.edges
        )
    }
}
