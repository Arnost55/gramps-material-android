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
    suspend fun getPersonFromNetwork(handle: String): GrampsPerson = getPerson(handle, recordView = true)

    suspend fun getPersonForGraph(handle: String): GrampsPerson = getPerson(handle, recordView = false)

    private suspend fun getPerson(handle: String, recordView: Boolean): GrampsPerson {
        val person = api().getPerson(handle, "all")
        dbProvider.personDao.insertPerson(person)
        if (recordView) recordRecent(person)
        return person
    }

    suspend fun getFamilyFromNetwork(handle: String): GrampsFamily {
        return dbProvider.familyDao.getFamilyByHandle(handle)
            ?: api().getFamily(handle, "all").also { dbProvider.familyDao.insertFamily(it) }
    }

    suspend fun loadAllFamiliesFromNetwork(): List<GrampsFamily> {
        val pageSize = 50
        val families = linkedMapOf<String, GrampsFamily>()
        var page = 1
        while (true) {
            val response = api().getFamilies(page = page, pageSize = pageSize)
            val added = response.count { family -> families.put(family.handle, family) == null }
            if (response.isEmpty() || response.size < pageSize || added == 0) break
            page++
        }
        val result = families.values.toList()
        if (result.isNotEmpty()) dbProvider.familyDao.insertAllFamilies(*result.toTypedArray())
        return result
    }

    suspend fun getAllCachedFamilies(): List<GrampsFamily> = dbProvider.familyDao.getAllFamilies()

    suspend fun getPeopleBookmarks(): Set<String> = api().getPeopleBookmarks().toSet()

    suspend fun setPersonBookmarked(handle: String, bookmarked: Boolean) {
        val response = if (bookmarked) api().addPeopleBookmark(handle) else api().removePeopleBookmark(handle)
        if (!response.isSuccessful) throw IllegalStateException("Could not update bookmark")
    }

    suspend fun getRelationship(firstHandle: String, secondHandle: String) =
        api().getRelationship(firstHandle, secondHandle)

    suspend fun getPersonTimeline(handle: String) = api().getPersonTimeline(handle)

    suspend fun getCitation(handle: String) = api().getCitation(handle)

    suspend fun getSource(handle: String) = api().getSource(handle)

    suspend fun getNote(handle: String) = api().getNote(handle)

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

    /**
     * Loads every page exposed by Gramps Web. The API does not include a total in this response,
     * so a short page (or a page that adds no new handles) terminates pagination safely.
     */
    suspend fun loadAllPeopleFromNetwork(): List<GrampsPerson> {
        val pageSize = 50
        val allPeople = linkedMapOf<String, GrampsPerson>()
        var page = 1
        while (true) {
            val response = api().getPeople(page = page, pageSize = pageSize, profile = "self")
            val added = response.count { person -> allPeople.put(person.handle, person) == null }
            if (response.isEmpty() || response.size < pageSize || added == 0) break
            page++
        }
        val people = allPeople.values.toList()
        if (people.isNotEmpty()) dbProvider.personDao.insertAllPeople(*people.toTypedArray())
        return people
    }

    suspend fun loadFirstPeoplePage(): List<GrampsPerson> = loadAllPeopleFromNetwork()

    suspend fun getCachedPerson(handle: String): GrampsPerson? {
        return dbProvider.personDao.getPersonByHandle(handle)
    }

    suspend fun cachePerson(person: GrampsPerson) {
        dbProvider.personDao.insertPerson(person)
    }

    suspend fun cachePeople(vararg people: GrampsPerson) {
        dbProvider.personDao.insertAllPeople(*people)
    }

    suspend fun getAllCachedPeople(): List<GrampsPerson> = dbProvider.personDao.getAllPeople()

    suspend fun getCachedPersonCount(): Int = dbProvider.personDao.getPersonCount()

    suspend fun searchCachedPeople(query: String): List<SearchResult> {
        val needle = query.trim()
        if (needle.isBlank()) return emptyList()
        return dbProvider.personDao.getAllPeople()
            .asSequence()
            .filter { it.displayName().contains(needle, ignoreCase = true) }
            .map { person -> SearchResult(handle = person.handle, object_type = "person", `object` = person) }
            .toList()
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
        dbProvider.familyDao.deleteAllFamilies()
        dbProvider.treeDao.deleteAllTrees()
        dbProvider.recentPeopleDao.clear()
    }
}