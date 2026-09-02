package app.grampsmaterial.core_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.grampsmaterial.core_network.models.GrampsPerson

@Dao
interface PersonDao {
    @Query("SELECT * FROM people WHERE handle = :handle")
    suspend fun getPersonByHandle(handle: String): GrampsPerson?

    @Query("SELECT * FROM people")
    suspend fun getAllPeople(): List<GrampsPerson>

    @Query("SELECT COUNT(*) FROM people")
    suspend fun getPersonCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: GrampsPerson): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPeople(vararg people: GrampsPerson): LongArray

    @Query("DELETE FROM people")
    suspend fun deleteAllPeople(): Int
}