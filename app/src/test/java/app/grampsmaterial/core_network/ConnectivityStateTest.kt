package app.grampsmaterial.core_network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectivityStateTest {
    @Test
    fun idleHttpClient_doesNotBecomeUnreachable() {
        val tracker = ServerReachabilityTracker()
        tracker.reachable()

        // No heartbeat or timeout transition occurs while no HTTP request is active.
        assertTrue(tracker.state.value is ServerReachability.Reachable)
    }

    @Test
    fun successfulResponse_marksServerReachable() {
        assertEquals(HttpOutcome.Success, classifyHttpOutcome(200))
    }

    @Test
    fun unauthorizedResponse_isAuthenticationFailure_notReachabilityFailure() {
        assertEquals(HttpOutcome.AuthenticationFailure, classifyHttpOutcome(401))
    }

    @Test
    fun serverError_isDistinctFromAuthenticationFailure() {
        assertEquals(HttpOutcome.ServerFailure, classifyHttpOutcome(503))
    }
}
