package com.example.pushpull

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.pushpull.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // binding.root przez lateinit

        //zmiana nawigacji po zalogowaniu
        firebaseAuth = FirebaseAuth.getInstance()

        navController = findNavController(R.id.nav_host_fragment)
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.nav_graph)

        // Destynacja startowa gdy użytkownik jest zalogowany
        navGraph.setStartDestination(
            if (firebaseAuth.currentUser != null) {
                R.id.homeFragment // Użytkownik zalogowany
            } else {
                R.id.signUpFragment // Użytkownik niezalogowany
            }
        )

        navController.graph = navGraph


        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNav)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)





        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.signInFragment, R.id.signUpFragment -> supportActionBar?.hide()
                else -> supportActionBar?.show()
            }
        }

        // Ustaw OnDestinationChangedListener, aby ukryć BottomNavigationView dla określonych fragmentów
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.signUpFragment || destination.id == R.id.signInFragment) {
                // Ukryj BottomNavigationView, gdy jesteśmy na SignUpFragment
                bottomNavigation.visibility = View.GONE
            } else {
                // W przeciwnym razie upewnij się, że BottomNavigationView jest widoczny
                bottomNavigation.visibility = View.VISIBLE
            }


        }
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
                //tu dodajemy nowe menu itemy
                R.id.historyFragment, R.id.tipsFragment, R.id.userFragment -> {
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
                || destination.id == R.id.homeFragment
                || destination.id == R.id.userFragment
            ) {
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
            .setPopUpTo(R.id.nav_graph, false)
            .setLaunchSingleTop(true)
        navController.navigate(R.id.homeFragment, null, builder.build())
    }


}
