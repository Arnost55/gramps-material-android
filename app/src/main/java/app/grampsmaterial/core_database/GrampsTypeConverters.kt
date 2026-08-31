package app.grampsmaterial.core_database

import androidx.room.TypeConverter
import app.grampsmaterial.core_network.models.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GrampsTypeConverters {
    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    @TypeConverter
    fun fromGrampsName(value: GrampsName?): String? = value?.let { json.encodeToString(it) }
    @TypeConverter
    fun toGrampsName(value: String?): GrampsName? = value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromGrampsNameList(value: List<GrampsName>): String = json.encodeToString(value)
    @TypeConverter
    fun toGrampsNameList(value: String): List<GrampsName> = json.decodeFromString(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)
    @TypeConverter
    fun toStringList(value: String): List<String> = json.decodeFromString(value)

    @TypeConverter
    fun fromGrampsEventRefList(value: List<GrampsEventRef>): String = json.encodeToString(value)
    @TypeConverter
    fun toGrampsEventRefList(value: String): List<GrampsEventRef> = json.decodeFromString(value)

    @TypeConverter
    fun fromGrampsMediaRefList(value: List<GrampsMediaRef>): String = json.encodeToString(value)
    @TypeConverter
    fun toGrampsMediaRefList(value: String): List<GrampsMediaRef> = json.decodeFromString(value)

    @TypeConverter
    fun fromPersonProfile(value: PersonProfile?): String? = value?.let { json.encodeToString(it) }
    @TypeConverter
    fun toPersonProfile(value: String?): PersonProfile? = value?.let { json.decodeFromString(it) }
}