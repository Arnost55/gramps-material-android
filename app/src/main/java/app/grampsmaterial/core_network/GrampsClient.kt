package app.grampsmaterial.core_network

import android.util.Log
import app.grampsmaterial.BuildConfig
import app.grampsmaterial.core_database.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val TAG = "GrampsClient"

class GrampsClient(private val sessionManager: SessionManager) {

    private var serverUrl: String = ""
    private var allowInsecureHttp: Boolean = false
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = false
    }

    init {
        // Keep in-memory cache updated
        CoroutineScope(Dispatchers.IO).launch {
            sessionManager.serverUrlFlow.collect { url ->
                serverUrl = url.trim().trimEnd('/')
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            sessionManager.allowInsecureHttpFlow.collect { allow ->
                allowInsecureHttp = allow
            }
        }
    }

    fun getApiService(
        customUrl: String? = null,
        allowInsecureOverride: Boolean? = null
    ): GrampsApiService {
        val urlWithProtocol = normalizedUrl(customUrl)
        val insecureAllowed = allowInsecureOverride ?: allowInsecureHttp

        if (urlWithProtocol.startsWith("http://", ignoreCase = true) && !insecureAllowed) {
            // Throw configuration error if HTTP is used but not explicitly allowed
            throw SecurityException("Insecure HTTP connections are disabled. Enable 'Allow insecure local server' in settings to use HTTP.")
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }

        val authInterceptor = Interceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()
            if (!request.url.encodedPath.endsWith("/api/token/")) {
                val token = sessionManager.getAccessToken()
                if (!token.isNullOrEmpty()) {
                    builder.header("Authorization", "Bearer $token")
                }
            }
            chain.proceed(builder.build())
        }

        val okHttpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)


        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl("$urlWithProtocol/")
            .client(okHttpClientBuilder.build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(GrampsApiService::class.java)
    }

    fun getProbeApi(serverUrl: String, allowInsecureOverride: Boolean = false): GrampsProbeApi {
        val normalizedUrl = normalizedUrl(serverUrl)
        if (normalizedUrl.startsWith("http://", ignoreCase = true) && !allowInsecureOverride) {
            throw SecurityException("Insecure HTTP connections are disabled.")
        }
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("$normalizedUrl/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GrampsProbeApi::class.java)
    }

    private fun normalizedUrl(customUrl: String?): String {
        return GrampsServer.normalizeUrl(customUrl ?: serverUrl)
    }

}
