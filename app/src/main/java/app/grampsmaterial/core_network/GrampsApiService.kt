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

    @PUT("api/users/-/")
    suspend fun selectCurrentUserTree(@Body request: TreeSelectionRequest): Response<Unit>

    @POST("api/token/refresh/")
    suspend fun refreshToken(@Header("Authorization") refreshAuthorization: String): Response<TokenResponse>

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

    @GET("api/bookmarks/people")
    suspend fun getPeopleBookmarks(): List<String>

    @PUT("api/bookmarks/people/{handle}")
    suspend fun addPeopleBookmark(@Path("handle") handle: String): Response<Unit>

    @DELETE("api/bookmarks/people/{handle}")
    suspend fun removePeopleBookmark(@Path("handle") handle: String): Response<Unit>

    @GET("api/relations/{handle1}/{handle2}")
    suspend fun getRelationship(
        @Path("handle1") handle1: String,
        @Path("handle2") handle2: String
    ): Relationship

    @GET("api/people/{handle}/timeline")
    suspend fun getPersonTimeline(
        @Path("handle") handle: String,
        @Query("page") page: Int = 1,
        @Query("pagesize") pageSize: Int = 100
    ): List<TimelineEvent>

    @GET("api/citations/{handle}")
    suspend fun getCitation(
        @Path("handle") handle: String,
        @Query("profile") profile: String = "all"
    ): GrampsCitation

    @GET("api/sources/{handle}")
    suspend fun getSource(
        @Path("handle") handle: String,
        @Query("profile") profile: String = "all"
    ): GrampsSource

    @GET("api/notes/{handle}")
    suspend fun getNote(
        @Path("handle") handle: String,
        @Query("profile") profile: String = "all"
    ): GrampsNote

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
