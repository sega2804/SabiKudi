package com.crypticsamsara.sabikudi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crypticsamsara.sabikudi.data.preferences.AppMode
import com.crypticsamsara.sabikudi.data.preferences.AppPreferencesRepository
import com.crypticsamsara.sabikudi.data.preferences.AppRole
import com.crypticsamsara.sabikudi.data.preferences.AppTheme
import com.crypticsamsara.sabikudi.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences(
                mode = null,
                businessRole = AppRole.OWNER,
                sharedRole = AppRole.ORGANISER,
                theme = AppTheme.SYSTEM,
                isAppLockEnabled = false,
                appPin = "1234",
                autoPurgeImages = true,
                retentionDays = 30,
                cameraPermissionGranted = false
            )
        )

    fun selectMode(mode: AppMode) {
        viewModelScope.launch {
            preferencesRepository.setMode(mode)
        }
    }

    fun selectRole(mode: AppMode, role: AppRole) {
        viewModelScope.launch {
            preferencesRepository.setRoleForMode(mode, role)
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.setTheme(theme)
        }
    }

    fun resetModeAndRole() {
        viewModelScope.launch {
            preferencesRepository.clearAll()
        }
    }
}
