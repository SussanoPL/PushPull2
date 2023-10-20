package com.example.pushpull

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.pushpull.data.Exercise
import com.example.pushpull.data.ExerciseDao
import com.example.pushpull.data.GymDatabase
import com.example.pushpull.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
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
        setContentView(binding.root) // binding.root przez lateinit

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNav)
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        NavigationUI.setupWithNavController(bottomNavigation, navController)

        // Inicjalizacja bazy danych i DAO w metodzie onCreate
       // gymDatabase = GymDatabase.getDatabase(this)
        //exerciseDao = gymDatabase.exerciseDao()



    }
}
