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

        val exercise1 = Exercise(
            name = "Wyciskanie hantli na ławce pochyłej",
            muscleGroup = "Klatka piersiowa",
            equipment = "Hantle"
        )
        val exercise2 = Exercise(
            name = "Wyciskanie sztangi na ławce pochyłej",
            muscleGroup = "Klatka piersiowa",
            equipment = "Sztanga"
        )




        GlobalScope.launch(Dispatchers.IO) {

            exerciseDao.insertExercise(exercise1)
            exerciseDao.insertExercise(exercise2)




        }
    }
}
