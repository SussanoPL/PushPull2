package com.example.pushpull.data

import java.util.Date

data class History(
    val date: Date? = null,
    val day: String? = null,
    val docId: String? = null,
    val exercises: List<Exercises> = emptyList(),
    val name: String? = null,
    val notes: String? = null,
    val trainingTime: Long? = null,
    val userId: String? = null
)

data class Exercises(
    val exerciseId: String? = null,
    val repetitions: Int? = null,
    val sets: Int? = null,
    val weight: Double? = null
)


