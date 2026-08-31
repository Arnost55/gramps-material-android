package app.grampsmaterial.core_network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlNormalizationTest {
    @Test fun addsHttpsAndOneTrailingSlashIsRemoved() {
        assertEquals("https://family.example.test", GrampsServer.normalizeUrl(" family.example.test/// "))
    }

    @Test fun preservesExplicitHttpForTheCallerToAuthorize() {
        val url = GrampsServer.normalizeUrl("http://192.168.1.4:5000/")
        assertEquals("http://192.168.1.4:5000", url)
        assertTrue(GrampsServer.isInsecure(url))
        assertFalse(GrampsServer.isInsecure("https://family.example.test"))
    }
}
