package com.example.pushpull

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.pushpull.data.FirebaseExercise
import com.example.pushpull.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // binding.root przez lateinit




        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNav)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    popEverythingAndNavigateToHost()
                    true
                }
                else -> false
            }
        }
        NavigationUI.setupActionBarWithNavController(this, navController)

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    popEverythingAndNavigateToHost()
                    true
                }
                R.id.historyFragment, R.id.tipsFragment -> {
                    val navController = findNavController(R.id.nav_host_fragment)
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    true
                }
                else -> false
            }
        }







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

    private fun popEverythingAndNavigateToHost() {
        val builder = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, false)  // Używam `nav_graph` jako domyślnego ID dla całego grafu nawigacji. Zamień to na właściwe ID twojego grafu nawigacji, jeśli jest inne.
            .setLaunchSingleTop(true)
        navController.navigate(R.id.homeFragment, null, builder.build())
    }




}
