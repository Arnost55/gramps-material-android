package app.grampsmaterial.core_network

import app.grampsmaterial.core_database.GrampsDatabaseProvider
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.models.GrampsTree
import app.grampsmaterial.core_network.models.TreeSelectionRequest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
class TreeRepository @Inject constructor(
    private val grampsClient: GrampsClient,
    private val sessionManager: SessionManager,
    private val dbProvider: GrampsDatabaseProvider
) {
    suspend fun cachedTrees(): List<GrampsTree> {
        val cached = dbProvider.treeDao.getAllTrees()
        if (cached.isNotEmpty()) {
            return cached
        }
        
        val trees = getTreesFromNetwork()
        dbProvider.treeDao.insertAllTrees(*trees.toTypedArray())
        
        return trees
    }

    suspend fun getTreesFromNetwork(): List<GrampsTree> {
        val serverUrl = sessionManager.serverUrlFlow.first()
        return grampsClient.getApiService(serverUrl).getTrees()
    }

    /** Selects the active multi-tree database on the server, then refreshes the JWT claim. */
    suspend fun saveSelectedTree(treeId: String, treeName: String) {
        val api = grampsClient.getApiService(sessionManager.serverUrlFlow.first())
        val selection = api.selectCurrentUserTree(TreeSelectionRequest(treeId))
        if (!selection.isSuccessful) throw HttpException(selection)

        val refreshToken = requireNotNull(sessionManager.getRefreshToken()) { "Session refresh token is missing" }
        val refreshed = api.refreshToken("Bearer $refreshToken")
        if (!refreshed.isSuccessful) throw HttpException(refreshed)
        sessionManager.saveTokens(requireNotNull(refreshed.body()).access_token, refreshToken)
        sessionManager.saveSelectedTree(treeId, treeName)
    }

    suspend fun getSelectedTreeId(): String = sessionManager.selectedTreeIdFlow.first()
    suspend fun getSelectedTreeName(): String = sessionManager.selectedTreeNameFlow.first()
}
