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
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()

    val personDao: PersonDao = database.personDao()
    val familyDao: FamilyDao = database.familyDao()
    val treeDao: TreeDao = database.treeDao()
    val recentPeopleDao: RecentPeopleDao = database.recentPeopleDao()
    val pendingMutationDao: PendingMutationDao = database.pendingMutationDao()

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS families (" +
                        "handle TEXT NOT NULL, gramps_id TEXT, father_handle TEXT, mother_handle TEXT, " +
                        "child_ref_list TEXT NOT NULL, type TEXT, profile TEXT, PRIMARY KEY(handle))"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS pending_person_name_mutations (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, handle TEXT NOT NULL, " +
                        "firstName TEXT NOT NULL, surname TEXT NOT NULL, createdAt INTEGER NOT NULL)"
                )
            }
        }
    }
}
