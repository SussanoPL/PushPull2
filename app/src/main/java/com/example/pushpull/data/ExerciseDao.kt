package com.example.pushpull.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insertExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("SELECT * FROM EXERCISE")
    suspend fun getAllExercises(): List<Exercise>

    @Query("SELECT * FROM EXERCISE WHERE muscleGroup = :muscleGroup")
    suspend fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<Exercise>>
}