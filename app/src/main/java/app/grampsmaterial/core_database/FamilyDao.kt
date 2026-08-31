package app.grampsmaterial.core_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.grampsmaterial.core_network.models.GrampsFamily

@Dao
interface FamilyDao {
    @Query("SELECT * FROM families WHERE handle = :handle")
    suspend fun getFamilyByHandle(handle: String): GrampsFamily?

    @Query("SELECT * FROM families")
    suspend fun getAllFamilies(): List<GrampsFamily>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: GrampsFamily)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFamilies(vararg families: GrampsFamily)

    @Query("DELETE FROM families")
    suspend fun deleteAllFamilies()
}
