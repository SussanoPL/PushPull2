package com.example.pushpull.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface WorkoutPlanDao {
    @Insert
    suspend fun insertPlan(workoutPlan: WorkoutPlan)

    @Update
    suspend fun updatePlan(workoutPlan: WorkoutPlan)

    @Delete
    suspend fun deletePlan(workoutPlan: WorkoutPlan)

    @Query("SELECT * FROM WORKOUT_PLAN")
    suspend fun getAllPlans(): List<WorkoutPlan>
}