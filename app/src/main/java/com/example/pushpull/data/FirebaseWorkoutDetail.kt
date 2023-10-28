package com.example.pushpull.data

data class FirebaseWorkoutDetail(
    val id: String = "",
    val workoutPlanId: String = "",
    val exerciseId: String = "",
    val sets: Int = 0,
    val repetitions: Int = 0,
    val weight: Float = 0f
)
