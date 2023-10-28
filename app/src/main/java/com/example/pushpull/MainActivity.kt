package com.example.pushpull

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.pushpull.data.FirebaseExercise
import com.example.pushpull.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController


    private lateinit var dbRef : DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // binding.root przez lateinit
        dbRef = FirebaseDatabase.getInstance().getReference("Exercise")

        // Dodanie testowego ćwiczenia do Firebase
        val exerciseId = dbRef.push().key!!
        val testExercise = FirebaseExercise(
            id = exerciseId,
            name = "Pompki",
            muscleGroup = "Klatka piersiowa",
            equipment = "Brak",
            description = "Ćwiczenie polegające na podnoszeniu ciała przy pomocy rąk."
        )
/*        dbref.child(exerciseid).setvalue(testexercise)
            .addonsuccesslistener {
                log.d("firebasesuccess", "ćwiczenie dodane pomyślnie!")
            }
            .addonfailurelistener { exception ->
                log.e("firebaseerror", "błąd podczas dodawania ćwiczenia: ", exception)
            }*/

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNav)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        NavigationUI.setupWithNavController(bottomNavigation, navController)
        NavigationUI.setupActionBarWithNavController(this, navController)




        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.historyFragment
                || destination.id == R.id.tipsFragment
                || destination.id == R.id.homeFragment) {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            } else {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }



    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }




}
