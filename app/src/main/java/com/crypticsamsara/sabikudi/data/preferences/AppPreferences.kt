package com.crypticsamsara.sabikudi.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sabikudi_preferences")

enum class AppMode {
    BUSINESS, SHARED
}

enum class AppRole {
    OWNER, STAFF, ORGANISER, MEMBER;

    companion object {
        fun defaultForMode(mode: AppMode): AppRole = when (mode) {
            AppMode.BUSINESS -> OWNER
            AppMode.SHARED -> ORGANISER
        }
    }
}

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

data class UserPreferences(
    val mode: AppMode?,
    val businessRole: AppRole,
    val sharedRole: AppRole,
    val theme: AppTheme,
    val isAppLockEnabled: Boolean,
    val appPin: String,
    val autoPurgeImages: Boolean,
    val retentionDays: Int,
    val cameraPermissionGranted: Boolean
) {
    fun currentRoleForMode(): AppRole {
        return when (mode) {
            AppMode.BUSINESS -> businessRole
            AppMode.SHARED -> sharedRole
            null -> AppRole.ORGANISER
        }
    }
}

@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val APP_MODE = stringPreferencesKey("app_mode")
        val BUSINESS_ROLE = stringPreferencesKey("business_role")
        val SHARED_ROLE = stringPreferencesKey("shared_role")
        val APP_THEME = stringPreferencesKey("app_theme")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_PIN = stringPreferencesKey("app_pin")
        val AUTO_PURGE = booleanPreferencesKey("auto_purge")
        val RETENTION_DAYS = intPreferencesKey("retention_days")
        val CAMERA_PERM = booleanPreferencesKey("camera_perm")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[Keys.APP_MODE]
        val mode = modeStr?.let { runCatching { AppMode.valueOf(it) }.getOrNull() }

        val busRoleStr = preferences[Keys.BUSINESS_ROLE] ?: AppRole.OWNER.name
        val busRole = runCatching { AppRole.valueOf(busRoleStr) }.getOrDefault(AppRole.OWNER)

        val shrRoleStr = preferences[Keys.SHARED_ROLE] ?: AppRole.ORGANISER.name
        val shrRole = runCatching { AppRole.valueOf(shrRoleStr) }.getOrDefault(AppRole.ORGANISER)

        val themeStr = preferences[Keys.APP_THEME] ?: AppTheme.SYSTEM.name
        val theme = runCatching { AppTheme.valueOf(themeStr) }.getOrDefault(AppTheme.SYSTEM)

        val isLockEnabled = preferences[Keys.APP_LOCK_ENABLED] ?: false
        val pin = preferences[Keys.APP_PIN] ?: "1234"
        val autoPurge = preferences[Keys.AUTO_PURGE] ?: true
        val retentionDays = preferences[Keys.RETENTION_DAYS] ?: 30
        val cameraPerm = preferences[Keys.CAMERA_PERM] ?: false

        UserPreferences(
            mode = mode,
            businessRole = busRole,
            sharedRole = shrRole,
            theme = theme,
            isAppLockEnabled = isLockEnabled,
            appPin = pin,
            autoPurgeImages = autoPurge,
            retentionDays = retentionDays,
            cameraPermissionGranted = cameraPerm
        )
    }

    suspend fun setMode(mode: AppMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.APP_MODE] = mode.name
        }
    }

    suspend fun setRoleForMode(mode: AppMode, role: AppRole) {
        context.dataStore.edit { preferences ->
            when (mode) {
                AppMode.BUSINESS -> preferences[Keys.BUSINESS_ROLE] = role.name
                AppMode.SHARED -> preferences[Keys.SHARED_ROLE] = role.name
            }
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[Keys.APP_THEME] = theme.name
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setAppPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.APP_PIN] = pin
        }
    }

    suspend fun setAutoPurge(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.AUTO_PURGE] = enabled
        }
    }

    suspend fun setRetentionDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.RETENTION_DAYS] = days
        }
    }

    suspend fun setCameraPermissionGranted(granted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CAMERA_PERM] = granted
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
