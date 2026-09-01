package app.grampsmaterial.core_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingMutationDao {
    @Query("SELECT * FROM pending_person_name_mutations ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingPersonNameMutationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mutation: PendingPersonNameMutationEntity)

    @Query("DELETE FROM pending_person_name_mutations WHERE id = :id")
    suspend fun delete(id: Long)
}
