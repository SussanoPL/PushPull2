package com.example.pushpull.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutHistoryDao {
    @Insert
    suspend fun insertHistory(workoutHistory: WorkoutHistory)

    @Update
    suspend fun updateHistory(workoutHistory: WorkoutHistory)

    @Delete
    suspend fun deleteHistory(workoutHistory: WorkoutHistory)

    @Query("SELECT * FROM WORKOUT_HISTORY")
    suspend fun getAllHistories(): List<WorkoutHistory>
}