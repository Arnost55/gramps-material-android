package app.grampsmaterial.core_database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.grampsmaterial.core_network.models.GrampsPerson
import app.grampsmaterial.core_network.models.GrampsTree

@Database(entities = [GrampsPerson::class, GrampsTree::class, RecentPersonEntity::class], version = 2, exportSchema = false)
@TypeConverters(GrampsTypeConverters::class)
abstract class GrampsDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun treeDao(): TreeDao
    abstract fun recentPeopleDao(): RecentPeopleDao
}