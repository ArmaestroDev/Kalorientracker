package de.armando.kalorientracker.data

import de.armando.kalorientracker.data.model.ActivityEntry
import de.armando.kalorientracker.data.model.DailyLog
import de.armando.kalorientracker.data.model.FoodEntry
import de.armando.kalorientracker.data.db.LogDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class LogRepository(private val logDao: LogDao) {

    fun getDailyLog(date: LocalDate): Flow<DailyLog> {
        val foodFlow = logDao.getFoodEntriesForDate(date)
        val activityFlow = logDao.getActivityEntriesForDate(date)
        return combine(foodFlow, activityFlow) { foodEntries, activityEntries ->
            DailyLog(foodEntries, activityEntries)
        }
    }

    suspend fun addFoodEntry(foodEntry: FoodEntry) {
        logDao.insertFoodEntry(foodEntry)
    }

    suspend fun addActivityEntry(activityEntry: ActivityEntry) {
        logDao.insertActivityEntry(activityEntry)
    }

    suspend fun updateFoodEntry(foodEntry: FoodEntry) {
        logDao.updateFoodEntry(foodEntry)
    }
    suspend fun updateActivityEntry(activityEntry: ActivityEntry) {
        logDao.updateActivityEntry(activityEntry)
    }

    suspend fun deleteFoodEntry(foodEntry: FoodEntry) {
        logDao.deleteFoodEntry(foodEntry)
    }

    suspend fun deleteActivityEntry(activityEntry: ActivityEntry) {
        logDao.deleteActivityEntry(activityEntry)
    }
}