package app.grampsmaterial.core_network.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val access_token: String,
    val refresh_token: String? = null
)

@Serializable
data class TokenRequest(
    val username: String,
    val password: String
)

@Entity(tableName = "trees")
@Serializable
data class GrampsTree(
    @PrimaryKey val id: String,
    val name: String,
    val enabled: Boolean = true
)

@Serializable
data class DatabaseInfo(
    val name: String? = null,
    val type: String? = null
)

@Serializable
data class ObjectCounts(
    val people: Int = 0,
    val families: Int = 0,
    val events: Int = 0,
    val places: Int = 0,
    val media: Int = 0
)

@Serializable
data class MetadataResponse(
    val database: DatabaseInfo? = null,
    val default_person: String? = null,
    val object_counts: ObjectCounts? = null
)

@Serializable
data class GrampsSurname(
    val surname: String? = null,
    val prefix: String? = null,
    val connector: String? = null,
    val primary: Boolean? = null
)

@Serializable
data class GrampsName(
    val first_name: String? = null,
    val surname_list: List<GrampsSurname> = emptyList(),
    val suffix: String? = null,
    val title: String? = null,
    val type: String? = null
)

@Serializable
data class GrampsDate(
    val text: String? = null,
    val year: Int? = null,
    val sortval: Long? = null
)

@Serializable
data class GrampsEventRef(
    val ref: String,
    val role: String? = null
)

@Serializable
data class GrampsMediaRef(
    val ref: String,
    val rect: List<Float> = emptyList()
)

@Serializable
data class EventProfile(
    val type: String? = null,
    val date: String? = null,
    val place: String? = null,
    val place_name: String? = null
)

@Serializable
data class PersonProfile(
    val birth: EventProfile? = null,
    val death: EventProfile? = null,
    val events: List<EventProfile> = emptyList(),
    val name_display: String? = null,
    val sex: String? = null,
    val handle: String? = null,
    val gramps_id: String? = null
)

@Serializable
data class FamilyProfile(
    val handle: String,
    val gramps_id: String? = null,
    val father: PersonProfile? = null,
    val mother: PersonProfile? = null,
    val children: List<PersonProfile> = emptyList(),
    val relationship: String? = null,
    val marriage: EventProfile? = null,
    val divorce: EventProfile? = null
)

@Entity(tableName = "people")
@Serializable
data class GrampsPerson(
    @PrimaryKey val handle: String,
    val gramps_id: String? = null,
    val gender: Int? = null,
    val primary_name: GrampsName? = null,
    val alternate_names: List<GrampsName> = emptyList(),
    val family_list: List<String> = emptyList(),
    val parent_family_list: List<String> = emptyList(),
    val event_ref_list: List<GrampsEventRef> = emptyList(),
    val media_list: List<GrampsMediaRef> = emptyList(),
    val note_list: List<String> = emptyList(),
    val citation_list: List<String> = emptyList(),
    val private: Boolean = false,
    val profile: PersonProfile? = null
) {
    // Event references are opaque handles, never user-facing dates.
    val birthDate: String?
        get() = profile?.birth?.date
    val deathDate: String?
        get() = profile?.death?.date
}

@Serializable
data class GrampsFamily(
    val handle: String,
    val gramps_id: String? = null,
    val father_handle: String? = null,
    val mother_handle: String? = null,
    val child_ref_list: List<ChildRef> = emptyList(),
    val type: String? = null,
    val profile: FamilyProfile? = null
)

@Serializable
data class ChildRef(
    val ref: String, // handle of child
    val frel: String? = null,
    val mrel: String? = null
)

@Serializable
data class SearchResult(
    val handle: String,
    val object_type: String,
    val score: Double? = null,
    val `object`: GrampsPerson? = null
)

fun GrampsPerson.displayName(): String {
    val name = primary_name
    val surname = name?.surname_list.orEmpty()
        .mapNotNull { it.surname?.takeIf(String::isNotBlank) }
        .joinToString(" ")
    return listOfNotNull(name?.first_name?.takeIf(String::isNotBlank), surname.takeIf(String::isNotBlank))
        .joinToString(" ")
        .ifBlank { profile?.name_display?.takeIf(String::isNotBlank) ?: gramps_id ?: "Unknown person" }
}

fun GrampsPerson.lifeYears(): String? = listOfNotNull(
    profile?.birth?.date?.takeIf(String::isNotBlank),
    profile?.death?.date?.takeIf(String::isNotBlank)
).takeIf { it.isNotEmpty() }?.joinToString(" – ")
