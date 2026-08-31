package app.grampsmaterial.di

import android.content.Context
import app.grampsmaterial.core_database.GrampsDatabaseProvider
import app.grampsmaterial.core_database.SessionManager
import app.grampsmaterial.core_network.AuthRepository
import app.grampsmaterial.core_network.GrampsApiService
import app.grampsmaterial.core_network.GrampsClient
import app.grampsmaterial.core_network.PersonRepository
import app.grampsmaterial.core_network.TreeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        sessionManager: SessionManager,
        json: Json
    ): Retrofit {
        val baseUrl = runBlocking {
            sessionManager.serverUrlFlow.first().removeSuffix("/")
        }.ifBlank { "http://localhost" }
        
        return Retrofit.Builder()
            .baseUrl("$baseUrl/api/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideGrampsApiService(retrofit: Retrofit): GrampsApiService {
        return retrofit.create(GrampsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGrampsClient(
        sessionManager: SessionManager
    ): GrampsClient {
        return GrampsClient(sessionManager)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        grampsClient: GrampsClient,
        sessionManager: SessionManager
    ): AuthRepository {
        return AuthRepository(grampsClient, sessionManager)
    }

    @Provides
    @Singleton
    fun providePersonRepository(
        grampsClient: GrampsClient,
        sessionManager: SessionManager,
        dbProvider: GrampsDatabaseProvider
    ): PersonRepository {
        return PersonRepository(grampsClient, sessionManager, dbProvider)
    }

    @Provides
    @Singleton
    fun provideTreeRepository(
        grampsClient: GrampsClient,
        sessionManager: SessionManager,
        dbProvider: GrampsDatabaseProvider
    ): TreeRepository {
        return TreeRepository(grampsClient, sessionManager, dbProvider)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGrampsDatabaseProvider(
        @ApplicationContext context: Context
    ): GrampsDatabaseProvider {
        return GrampsDatabaseProvider(context)
    }
    
    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager {
        return SessionManager(context)
    }
}