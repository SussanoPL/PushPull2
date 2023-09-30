package com.example.pushpull.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "workout_detail",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlan::class,
            parentColumns = ["id"],
            childColumns = ["workoutPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutPlanId: Long,
    val exerciseId: Long,
    val sets: Int,
    val repetitions: Int,
    val weight: Float
)