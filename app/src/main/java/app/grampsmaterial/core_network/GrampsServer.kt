package app.grampsmaterial.core_network

/** Canonical server URL handling shared by connection, probes, and Retrofit. */
data class GrampsServer(
    val baseUrl: String,
    val displayName: String = "My Gramps Web"
) {
    val normalizedBaseUrl: String get() = normalizeUrl(baseUrl)

    companion object {
        fun normalizeUrl(value: String): String {
            val trimmed = value.trim().trimEnd('/')
            require(trimmed.isNotEmpty()) { "Server URL is required" }
            val withScheme = if ("://" in trimmed) trimmed else "https://$trimmed"
            require(withScheme.startsWith("https://", ignoreCase = true) ||
                withScheme.startsWith("http://", ignoreCase = true)
            ) { "Use an HTTP or HTTPS server URL" }
            return withScheme
        }

        fun isInsecure(value: String): Boolean =
            normalizeUrl(value).startsWith("http://", ignoreCase = true)
    }
}
