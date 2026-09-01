package app.grampsmaterial.core_sync

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.PersonRepository
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PeopleCacheWorkerEntryPoint {
    fun sessionManager(): SessionManager
    fun personRepository(): PersonRepository
}

/** Refreshes the local people cache without blocking any screen. */
class PeopleCacheWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val dependencies = EntryPointAccessors.fromApplication(
            applicationContext,
            PeopleCacheWorkerEntryPoint::class.java
        )
        val sessionManager = dependencies.sessionManager()
        if (!sessionManager.isConnectedFlow.first() || sessionManager.getAccessToken().isNullOrBlank()) {
            return Result.success()
        }
        return try {
            dependencies.personRepository().apply {
                flushPendingPersonNameMutations()
                loadAllPeopleFromNetwork()
                loadAllFamiliesFromNetwork()
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
