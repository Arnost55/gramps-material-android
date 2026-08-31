package app.grampsmaterial.core_network

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET

@Serializable
data class ReadyResponse(val status: String)

interface GrampsProbeApi {
    @GET("ready")
    suspend fun ready(): Response<ReadyResponse>
}
