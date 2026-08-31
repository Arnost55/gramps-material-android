package app.grampsmaterial.core_network

import org.junit.Assert.assertEquals
import org.junit.Test

class GrampsServerTest {
    @Test
    fun normalizedBaseUrl_trimsWhitespaceAndTrailingSlashes() {
        val server = GrampsServer(" https://family.example.com/// ")

        assertEquals("https://family.example.com", server.normalizedBaseUrl)
    }
}
