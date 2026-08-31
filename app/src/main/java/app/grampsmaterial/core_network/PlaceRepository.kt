package app.grampsmaterial.core_network

import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.models.GrampsPlace
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepository @Inject constructor(
    private val client: GrampsClient,
    private val session: SessionManager
) {
    suspend fun getAllPlaces(): List<GrampsPlace> {
        val api = client.getApiService(session.serverUrlFlow.first())
        val places = linkedMapOf<String, GrampsPlace>()
        var page = 1
        while (true) {
            val response = api.getPlaces(page = page)
            response.forEach { places[it.handle] = it }
            if (response.size < 100) break
            page++
        }
        return places.values.toList()
    }
}
