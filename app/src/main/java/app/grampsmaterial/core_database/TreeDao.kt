package app.grampsmaterial.core_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.grampsmaterial.core_network.models.GrampsTree

@Dao
interface TreeDao {
    @Query("SELECT * FROM trees WHERE id = :treeId")
    suspend fun getTreeById(treeId: String): GrampsTree?

    @Query("SELECT * FROM trees")
    suspend fun getAllTrees(): List<GrampsTree>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTree(tree: GrampsTree): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTrees(vararg trees: GrampsTree): LongArray

    @Query("DELETE FROM trees WHERE id = :treeId")
    suspend fun deleteTree(treeId: String): Int

    @Query("DELETE FROM trees")
    suspend fun deleteAllTrees(): Int
}