package app.grampsmaterial.core_database

import androidx.room.Entity

@Entity(tableName = "recent_people", primaryKeys = ["treeId", "handle"])
data class RecentPersonEntity(
    val treeId: String,
    val handle: String,
    val displayName: String,
    val lifeYears: String?,
    val portraitRef: String?,
    val lastViewedAt: Long
)
