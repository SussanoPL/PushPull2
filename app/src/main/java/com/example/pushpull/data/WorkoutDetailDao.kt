package com.example.pushpull.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDetailDao {
    @Insert
    suspend fun insertDetail(workoutDetail: WorkoutDetail)

    @Update
    suspend fun updateDetail(workoutDetail: WorkoutDetail)

    @Delete
    suspend fun deleteDetail(workoutDetail: WorkoutDetail)

    @Query("SELECT * FROM WORKOUT_DETAIL")
    suspend fun getAllDetails(): List<WorkoutDetail>
}