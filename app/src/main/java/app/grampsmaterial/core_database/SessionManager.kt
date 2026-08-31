package app.grampsmaterial.core_database

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.GeneralSecurityException

private const val TAG = "SessionManager"
private const val SECURE_PREFS_FILE = "secure_gramps_prefs"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gramps_settings")

class SessionManager(private val context: Context) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs: SharedPreferences by lazy {
        try {
            createSecurePrefs()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences, resetting...", e)
            context.deleteSharedPreferences(SECURE_PREFS_FILE)
            createSecurePrefs()
        }
    }

    private fun createSecurePrefs(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Keys for non-sensitive preferences
    private object PreferencesKeys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val USERNAME = stringPreferencesKey("username")
        val SELECTED_TREE_ID = stringPreferencesKey("selected_tree_id")
        val SELECTED_TREE_NAME = stringPreferencesKey("selected_tree_name")
        val HOME_PERSON_HANDLE = stringPreferencesKey("home_person_handle")
        val ALLOW_INSECURE_HTTP = booleanPreferencesKey("allow_insecure_http")
        val IS_CONNECTED = booleanPreferencesKey("is_connected")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "system", "light", "dark"
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val AMOLED_MODE = booleanPreferencesKey("amoled_mode")
    }

    // Keys for secure SharedPreferences
    private object SecureKeys {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
    }

    // Secure token APIs
    fun saveTokens(accessToken: String, refreshToken: String?) {
        securePrefs.edit().apply {
            putString(SecureKeys.ACCESS_TOKEN, accessToken)
            if (refreshToken != null) {
                putString(SecureKeys.REFRESH_TOKEN, refreshToken)
            }
            apply()
        }
    }

    fun getAccessToken(): String? = securePrefs.getString(SecureKeys.ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = securePrefs.getString(SecureKeys.REFRESH_TOKEN, null)

    fun clearTokens() {
        securePrefs.edit().apply {
            remove(SecureKeys.ACCESS_TOKEN)
            remove(SecureKeys.REFRESH_TOKEN)
            apply()
        }
    }

    // Flow getters for UI
    val serverUrlFlow: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.SERVER_URL] ?: "" }
    val usernameFlow: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.USERNAME] ?: "" }
    val selectedTreeIdFlow: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.SELECTED_TREE_ID] ?: "" }
    val selectedTreeNameFlow: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.SELECTED_TREE_NAME] ?: "" }
    val homePersonHandleFlow: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.HOME_PERSON_HANDLE] ?: "" }
    val allowInsecureHttpFlow: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.ALLOW_INSECURE_HTTP] ?: false }
    val isConnectedFlow: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.IS_CONNECTED] ?: false }
    val themeModeFlow: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.THEME_MODE] ?: "system" }
    val dynamicColorsFlow: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.DYNAMIC_COLORS] ?: true }
    val amoledModeFlow: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.AMOLED_MODE] ?: false }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { it[PreferencesKeys.SERVER_URL] = url }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { it[PreferencesKeys.USERNAME] = username }
    }

    suspend fun saveSelectedTree(treeId: String, treeName: String) {
        context.dataStore.edit {
            it[PreferencesKeys.SELECTED_TREE_ID] = treeId
            it[PreferencesKeys.SELECTED_TREE_NAME] = treeName
        }
    }

    suspend fun saveHomePersonHandle(handle: String) {
        context.dataStore.edit { it[PreferencesKeys.HOME_PERSON_HANDLE] = handle }
    }

    suspend fun setAllowInsecureHttp(allow: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ALLOW_INSECURE_HTTP] = allow }
    }

    suspend fun setConnected(connected: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_CONNECTED] = connected }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLORS] = enabled }
    }

    suspend fun setAmoledMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AMOLED_MODE] = enabled }
    }

    suspend fun logout() {
        clearTokens()
        context.dataStore.edit {
            it[PreferencesKeys.IS_CONNECTED] = false
            it[PreferencesKeys.SELECTED_TREE_ID] = ""
            it[PreferencesKeys.SELECTED_TREE_NAME] = ""
            it[PreferencesKeys.HOME_PERSON_HANDLE] = ""
        }
    }
}
