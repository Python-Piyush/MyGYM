
package com.boss.mygym

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "gym_timer_settings"
)

class TimerSettings(private val context: Context) {

    suspend fun saveDuration(
        muscle: String,
        seconds: Int
    ) {
        val key = intPreferencesKey(muscle)

        context.dataStore.edit { preferences ->
            preferences[key] = seconds
        }
    }

    fun getDuration(muscle: String) =
        context.dataStore.data.map { preferences ->

            val defaults = mapOf(
                "Chest" to 120,
                "Back" to 90,
                "Biceps" to 180,
                "Triceps" to 120,
                "Shoulders" to 120,
                "Legs" to 180
            )

            preferences[intPreferencesKey(muscle)]
                ?: defaults[muscle]
                ?: 120
        }
}
