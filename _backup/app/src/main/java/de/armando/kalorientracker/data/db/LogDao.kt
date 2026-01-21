package de.armando.kalorientracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.armando.kalorientracker.data.model.ActivityEntry
import de.armando.kalorientracker.data.model.FoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface LogDao {
    @Query("SELECT * FROM food_entries WHERE date = :date")
    fun getFoodEntriesForDate(date: LocalDate): Flow<List<FoodEntry>>

    @Query("SELECT * FROM activity_entries WHERE date = :date")
    fun getActivityEntriesForDate(date: LocalDate): Flow<List<ActivityEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(foodEntry: FoodEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityEntry(activityEntry: ActivityEntry)

    @Update
    suspend fun updateFoodEntry(foodEntry: FoodEntry)

    @Update
    suspend fun updateActivityEntry(activityEntry: ActivityEntry)

    @Delete
    suspend fun deleteFoodEntry(foodEntry: FoodEntry)

    @Delete
    suspend fun deleteActivityEntry(activityEntry: ActivityEntry)
}