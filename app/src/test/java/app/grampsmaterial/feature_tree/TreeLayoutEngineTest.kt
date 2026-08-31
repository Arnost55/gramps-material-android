package app.grampsmaterial.feature_tree

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TreeLayoutEngineTest {
    @Test fun `layout puts each generation in a separate column`() {
        val layout = TreeLayoutEngine().layout(listOf(listOf("root"), listOf("mother", "father")))
        assertEquals(0f, layout.nodes.getValue("root").x)
        assertTrue(layout.nodes.getValue("mother").x > layout.nodes.getValue("root").x)
        assertEquals(layout.nodes.getValue("mother").x, layout.nodes.getValue("father").x)
    }

    @Test fun `layout has stable bounds for a root without parents`() {
        val layout = TreeLayoutEngine().layout(listOf(listOf("root")))
        assertTrue(layout.width > 0f)
        assertTrue(layout.height > 0f)
    }
}
