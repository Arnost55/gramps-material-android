package app.grampsmaterial.core_network

import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.models.GrampsReport
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val client: GrampsClient,
    private val session: SessionManager
) {
    suspend fun getReports(): List<GrampsReport> =
        client.getApiService(session.serverUrlFlow.first()).getReports()
}
