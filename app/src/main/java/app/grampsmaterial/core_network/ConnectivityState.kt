package app.grampsmaterial.core_network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface NetworkState {
    data object Online : NetworkState
    data object Offline : NetworkState
    data object Unknown : NetworkState
}

sealed interface ServerReachability {
    data object Unknown : ServerReachability
    data object Checking : ServerReachability
    data class Reachable(val checkedAt: Instant) : ServerReachability
    data class Unreachable(val reason: ReachabilityFailure, val checkedAt: Instant) : ServerReachability
}

enum class ReachabilityFailure { Network, Timeout, Tls, Server }

@Singleton
class NetworkStateMonitor @Inject constructor(@ApplicationContext context: Context) {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    private val _state = MutableStateFlow(currentState())
    val state: StateFlow<NetworkState> = _state.asStateFlow()

    init {
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = update()
            override fun onLost(network: Network) = update()
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = update()
        })
    }

    private fun update() { _state.value = currentState() }
    private fun currentState(): NetworkState {
        val capabilities = connectivityManager.activeNetwork?.let(connectivityManager::getNetworkCapabilities)
            ?: return NetworkState.Offline
        return if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) NetworkState.Online else NetworkState.Offline
    }
}

@Singleton
class ServerReachabilityTracker @Inject constructor() {
    private val _state = MutableStateFlow<ServerReachability>(ServerReachability.Unknown)
    val state: StateFlow<ServerReachability> = _state.asStateFlow()

    fun checking() { _state.value = ServerReachability.Checking }
    fun reachable() { _state.value = ServerReachability.Reachable(Instant.now()) }
    fun unreachable(reason: ReachabilityFailure) { _state.value = ServerReachability.Unreachable(reason, Instant.now()) }

    fun recordFailure(error: IOException) {
        unreachable(if (error is java.net.SocketTimeoutException) ReachabilityFailure.Timeout else ReachabilityFailure.Network)
    }
}

enum class HttpOutcome { Success, AuthenticationFailure, ServerFailure, Other }

fun classifyHttpOutcome(statusCode: Int): HttpOutcome = when {
    statusCode in 200..299 -> HttpOutcome.Success
    statusCode == 401 -> HttpOutcome.AuthenticationFailure
    statusCode >= 500 -> HttpOutcome.ServerFailure
    else -> HttpOutcome.Other
}
