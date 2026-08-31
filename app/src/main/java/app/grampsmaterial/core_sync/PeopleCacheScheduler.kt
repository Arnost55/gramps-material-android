package app.grampsmaterial.core_sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleCacheScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun enqueueRefresh() {
        val request = OneTimeWorkRequestBuilder<PeopleCacheWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            PEOPLE_CACHE_WORK,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    private companion object {
        const val PEOPLE_CACHE_WORK = "refresh_all_gramps_people"
    }
}
