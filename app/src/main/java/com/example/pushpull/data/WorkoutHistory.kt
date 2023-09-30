package com.example.pushpull.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "workout_history",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutDetail::class,
            parentColumns = ["id"],
            childColumns = ["workoutDetailId"],
            onDelete = ForeignKey.CASCADE
        )
    ])
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutDetailId: Long,
    val workoutDate: Long,
)
