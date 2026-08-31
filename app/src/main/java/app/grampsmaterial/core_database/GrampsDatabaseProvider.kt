package app.grampsmaterial.core_database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrampsDatabaseProvider @Inject constructor(context: Context) {
    val database: GrampsDatabase = Room.databaseBuilder(
        context.applicationContext,
        GrampsDatabase::class.java,
        "gramps_database"
    ).addMigrations(MIGRATION_1_2).build()

    val personDao: PersonDao = database.personDao()
    val treeDao: TreeDao = database.treeDao()
    val recentPeopleDao: RecentPeopleDao = database.recentPeopleDao()

    private companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS recent_people (" +
                        "treeId TEXT NOT NULL, handle TEXT NOT NULL, displayName TEXT NOT NULL, " +
                        "lifeYears TEXT, portraitRef TEXT, lastViewedAt INTEGER NOT NULL, " +
                        "PRIMARY KEY(treeId, handle))"
                )
            }
        }
    }
}
