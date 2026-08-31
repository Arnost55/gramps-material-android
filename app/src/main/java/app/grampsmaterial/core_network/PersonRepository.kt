package app.grampsmaterial.core_network

import app.grampsmaterial.core_database.GrampsDatabaseProvider
import app.grampsmaterial.core_database.RecentPersonEntity
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.models.displayName
import app.grampsmaterial.core_network.models.lifeYears
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import app.grampsmaterial.core_network.models.GrampsFamily
import app.grampsmaterial.core_network.models.GrampsPerson
import app.grampsmaterial.core_network.models.SearchResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonRepository @Inject constructor(
    private val grampsClient: GrampsClient,
    private val sessionManager: SessionManager,
    private val dbProvider: GrampsDatabaseProvider
) {
    private suspend fun api(): GrampsApiService =
        grampsClient.getApiService(sessionManager.serverUrlFlow.first())
    suspend fun getPersonFromNetwork(handle: String): GrampsPerson {
        val person = api().getPerson(handle, "all")
        dbProvider.personDao.insertPerson(person)
        recordRecent(person)
        return person
    }

    suspend fun getFamilyFromNetwork(handle: String): GrampsFamily = api().getFamily(handle, "all")

    suspend fun searchPeopleFromNetwork(query: String): List<SearchResult> {
        val results = api().search(
            query = query,
            type = "person",
            profile = "self",
            page = 1,
            pageSize = 20
        )
        results.mapNotNull { it.`object` }.takeIf { it.isNotEmpty() }?.let {
            dbProvider.personDao.insertAllPeople(*it.toTypedArray())
        }
        return results
    }

    suspend fun loadFirstPeoplePage(): List<GrampsPerson> {
        val people = api().getPeople()
        dbProvider.personDao.insertAllPeople(*people.toTypedArray())
        return people
    }

    suspend fun getCachedPerson(handle: String): GrampsPerson? {
        return dbProvider.personDao.getPersonByHandle(handle)
    }

    suspend fun cachePerson(person: GrampsPerson) {
        dbProvider.personDao.insertPerson(person)
    }

    suspend fun cachePeople(vararg people: GrampsPerson) {
        dbProvider.personDao.insertAllPeople(*people)
    }

    suspend fun getAllCachedPeople(): List<GrampsPerson> {
        return dbProvider.personDao.getAllPeople()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observeRecentPeople(limit: Int = 20): Flow<List<RecentPersonEntity>> =
        sessionManager.selectedTreeIdFlow.flatMapLatest { treeId ->
            dbProvider.recentPeopleDao.observeRecent(treeId, limit)
        }

    private suspend fun recordRecent(person: GrampsPerson) {
        val treeId = sessionManager.selectedTreeIdFlow.first()
        if (treeId.isBlank()) return
        dbProvider.recentPeopleDao.upsert(
            RecentPersonEntity(
                treeId = treeId,
                handle = person.handle,
                displayName = person.displayName(),
                lifeYears = person.lifeYears(),
                portraitRef = person.media_list.firstOrNull()?.ref,
                lastViewedAt = System.currentTimeMillis()
            )
        )
        dbProvider.recentPeopleDao.trim(treeId, keep = 20)
    }

    suspend fun clearCache() {
        dbProvider.personDao.deleteAllPeople()
        dbProvider.treeDao.deleteAllTrees()
        dbProvider.recentPeopleDao.clear()
    }
}