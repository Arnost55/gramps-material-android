package app.grampsmaterial.core_database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_person_name_mutations")
data class PendingPersonNameMutationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val handle: String,
    val firstName: String,
    val surname: String,
    val createdAt: Long = System.currentTimeMillis()
)
