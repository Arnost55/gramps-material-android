package app.grampsmaterial.core_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPeopleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(person: RecentPersonEntity)

    @Query("SELECT * FROM recent_people WHERE treeId = :treeId ORDER BY lastViewedAt DESC LIMIT :limit")
    fun observeRecent(treeId: String, limit: Int = 20): Flow<List<RecentPersonEntity>>

    @Query("DELETE FROM recent_people WHERE treeId = :treeId AND handle NOT IN (SELECT handle FROM recent_people WHERE treeId = :treeId ORDER BY lastViewedAt DESC LIMIT :keep)")
    suspend fun trim(treeId: String, keep: Int)

    @Query("DELETE FROM recent_people")
    suspend fun clear()
}
