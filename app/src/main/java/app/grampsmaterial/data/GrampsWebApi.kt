package app.grampsmaterial.data

/**
 * Boundary for the Gramps Web REST API.
 *
 * Keep transport/auth details out of the Compose UI so we can later support
 * password login, OIDC, token refresh, multiple servers and offline caching.
 */
interface GrampsWebApi {
    suspend fun verifyConnection(server: GrampsServer): Result<Unit>
}

class PlaceholderGrampsWebApi : GrampsWebApi {
    override suspend fun verifyConnection(server: GrampsServer): Result<Unit> {
        return if (server.normalizedBaseUrl.startsWith("https://")) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Use an HTTPS Gramps Web URL"))
        }
    }
}
