package app.grampsmaterial.core_network

import app.grampsmaterial.core_database.GrampsDatabaseProvider
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.models.GrampsTree
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

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

    suspend fun saveSelectedTree(treeId: String, treeName: String) {
        sessionManager.saveSelectedTree(treeId, treeName)
    }

    suspend fun getSelectedTreeId(): String = sessionManager.selectedTreeIdFlow.first()
    suspend fun getSelectedTreeName(): String = sessionManager.selectedTreeNameFlow.first()
}
