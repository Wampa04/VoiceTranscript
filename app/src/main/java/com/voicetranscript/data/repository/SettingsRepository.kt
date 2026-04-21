package com.voicetranscript.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.voicetranscript.AudioLanguage
import com.voicetranscript.WhisperModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    }

    val selectedModel: Flow<WhisperModel> = context.dataStore.data.map { preferences ->
        val modelName = preferences[PreferencesKeys.SELECTED_MODEL] ?: WhisperModel.TINY.name
        try {
            WhisperModel.valueOf(modelName)
        } catch (e: Exception) {
            WhisperModel.TINY
        }
    }

    val selectedLanguage: Flow<AudioLanguage> = context.dataStore.data.map { preferences ->
        val langName = preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: AudioLanguage.AUTO.name
        try {
            AudioLanguage.valueOf(langName)
        } catch (e: Exception) {
            AudioLanguage.AUTO
        }
    }

    suspend fun saveSelectedModel(model: WhisperModel) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_MODEL] = model.name
        }
    }

    suspend fun saveSelectedLanguage(language: AudioLanguage) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE] = language.name
        }
    }
}
