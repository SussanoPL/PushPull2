/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.pushpull

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pushpull.data.Exercise
import com.example.pushpull.data.ExerciseDao
import com.example.pushpull.data.GymDatabase
import com.example.pushpull.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var gymDatabase: GymDatabase
    lateinit var exerciseDao: ExerciseDao



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        // Inicjalizacja bazy danych i DAO w metodzie onCreate
        gymDatabase = GymDatabase.getDatabase(this)
        exerciseDao = gymDatabase.exerciseDao()
        val exercisesList = listOf(
            Exercise(name = "Przyciąganie drążka", equipment = "wyciąg", muscleGroup = "plecy"),
            Exercise(name = "Uginanie ramion", equipment = "hantle", muscleGroup = "biceps"),
            Exercise(name = "Wyciskanie na ławce pochyłej", equipment = "hantle", muscleGroup = "klatka"),
            // Dodaj więcej rekordów tutaj
        )


        GlobalScope.launch(Dispatchers.IO) {
/*            val sortedExercises = exerciseDao.getAllExercises().sortedByDescending { it.id }
            val recordsToDelete = sortedExercises.take(3)*/
            for (exercise in exercisesList) {
                exerciseDao.insertExercise(exercise)
            }

        }
    }

}
