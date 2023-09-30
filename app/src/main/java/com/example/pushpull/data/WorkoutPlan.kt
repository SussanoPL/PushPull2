package com.example.pushpull.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_plan")
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?
)