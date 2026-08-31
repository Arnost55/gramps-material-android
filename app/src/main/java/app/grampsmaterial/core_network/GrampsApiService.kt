package app.grampsmaterial.core_network

import app.grampsmaterial.core_network.models.*
import retrofit2.Response
import retrofit2.http.*

interface GrampsApiService {

    @POST("api/token/")
    suspend fun login(
        @Body request: TokenRequest
    ): Response<TokenResponse>

    @GET("api/metadata/")
    suspend fun getMetadata(
        @Query("surnames") includeSurnames: Boolean = false
    ): MetadataResponse

    @GET("api/trees/")
    suspend fun getTrees(): List<GrampsTree>

    @GET("api/search/")
    suspend fun search(
        @Query("query") query: String,
        @Query("type") type: String = "person",
        @Query("profile") profile: String = "all",
        @Query("page") page: Int = 1,
        @Query("pagesize") pageSize: Int = 50
    ): List<SearchResult>

    @GET("api/people/")
    suspend fun getPeople(
        @Query("page") page: Int = 1,
        @Query("pagesize") pageSize: Int = 50,
        @Query("profile") profile: String = "self"
    ): List<GrampsPerson>

    @GET("api/people/{handle}")
    suspend fun getPerson(
        @Path("handle") handle: String,
        @Query("profile") profile: String = "all"
    ): GrampsPerson

    @GET("api/families/")
    suspend fun getFamilies(
        @Query("page") page: Int = 1,
        @Query("pagesize") pageSize: Int = 50,
        @Query("profile") profile: String = "all"
    ): List<GrampsFamily>

    @GET("api/families/{handle}")
    suspend fun getFamily(
        @Path("handle") handle: String,
        @Query("profile") profile: String = "all"
    ): GrampsFamily

    @GET("api/oidc/config")
    suspend fun getOidcConfig(): Response<Unit> // simple connection test
}
