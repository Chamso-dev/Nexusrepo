package com.nexus.grocerypos.data.settings

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private const val SETTINGS_DATASTORE_NAME = "grocery_pos_settings"
private const val SESSION_DATASTORE_NAME = "grocery_pos_session"

val Context.settingsDataStore by preferencesDataStore(name = SETTINGS_DATASTORE_NAME)
val Context.sessionDataStore by preferencesDataStore(name = SESSION_DATASTORE_NAME)
