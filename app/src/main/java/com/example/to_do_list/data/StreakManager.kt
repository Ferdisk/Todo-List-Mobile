package com.example.to_do_list.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "streak_prefs")

@Singleton
class StreakManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyStreakCount = intPreferencesKey("streak_count")
    private val keyLastDate = stringPreferencesKey("last_completion_date")

    suspend fun recordCompletion(): Int {
        var newStreak = 1
        context.dataStore.edit { prefs ->
            val today: LocalDate = LocalDate.now()
            val lastDateStr: String? = prefs[keyLastDate]
            val currentStreak: Int = prefs[keyStreakCount] ?: 0

            newStreak = if (lastDateStr == null) {
                1
            } else {
                val lastDate: LocalDate = LocalDate.parse(lastDateStr)
                val gap: Long = today.toEpochDay() - lastDate.toEpochDay()
                when {
                    gap == 0L -> currentStreak
                    gap == 1L -> currentStreak + 1
                    else -> 1
                }
            }

            prefs[keyStreakCount] = newStreak
            prefs[keyLastDate] = today.toString()
        }
        return newStreak
    }

    suspend fun getStreak(): Int {
        return context.dataStore.data
            .map { prefs -> prefs[keyStreakCount] ?: 0 }
            .first()
    }
}
