package app.grampsmaterial.data

data class GrampsServer(
    val baseUrl: String,
    val displayName: String = "My Gramps Web"
) {
    val normalizedBaseUrl: String
        get() = baseUrl.trim().trimEnd('/')
}
