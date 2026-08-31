package app.grampsmaterial.core_network

import android.util.Log
import app.grampsmaterial.BuildConfig
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.models.TokenRequest
import app.grampsmaterial.core_network.models.TokenResponse
import java.io.IOException
import javax.net.ssl.SSLException
import retrofit2.HttpException

private const val TAG = "AuthRepository"

sealed interface ServerReadiness {
    data object Ready : ServerReadiness
    data class Unsupported(val httpStatus: Int?) : ServerReadiness
    data object Unreachable : ServerReadiness
    data object TlsFailure : ServerReadiness
}

class AuthRepository(
    private val grampsClient: GrampsClient,
    private val sessionManager: SessionManager
) {
    suspend fun login(
        serverUrl: String,
        request: TokenRequest,
        allowInsecureHttp: Boolean = false
    ): TokenResponse {
        val response = grampsClient.getApiService(serverUrl, allowInsecureHttp).login(request)
        if (!response.isSuccessful) {
            if (BuildConfig.DEBUG) Log.d(TAG, "POST /api/token/ status=${response.code()}")
            throw HttpException(response)
        }
        return requireNotNull(response.body()) { "Token endpoint returned an empty response body" }
    }

    fun saveTokens(accessToken: String, refreshToken: String?) =
        sessionManager.saveTokens(accessToken, refreshToken)

    fun getAccessToken(): String? = sessionManager.getAccessToken()
    fun getRefreshToken(): String? = sessionManager.getRefreshToken()
    fun clearTokens() = sessionManager.clearTokens()

    suspend fun verifyConnection(
        serverUrl: String,
        allowInsecureHttp: Boolean = false
    ): ServerReadiness = try {
        val response = grampsClient.getProbeApi(serverUrl, allowInsecureHttp).ready()
        if (BuildConfig.DEBUG) Log.d(TAG, "GET /ready status=${response.code()}")
        if (response.isSuccessful && response.body()?.status == "ready") {
            ServerReadiness.Ready
        } else {
            ServerReadiness.Unsupported(response.code())
        }
    } catch (_: SSLException) {
        ServerReadiness.TlsFailure
    } catch (_: IOException) {
        ServerReadiness.Unreachable
    }
}
