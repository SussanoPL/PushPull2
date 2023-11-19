package com.example.pushpull

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.pushpull.data.Workout
import com.example.pushpull.databinding.FragmentHomeBinding
import com.example.pushpull.viewmodels.HomeViewModel
import com.example.pushpull.viewmodels.SearchViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Random

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel.fetchWorkouts()


        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        viewModel.fetchWorkouts()

        viewModel.workouts.observe(viewLifecycleOwner) { workouts ->
            binding.scheduleContainer.removeAllViews()
            workouts.forEach { workout ->
                addScheduleToUI(workout) // Pass the Workout object directly
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        binding.exSearch.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_searchFragment)
        }

        binding.fabAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }



    }



    private fun addScheduleToUI(workout: Workout) {
        val context = context ?: return
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        val dayContainer = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.bottomMargin = (12 * resources.displayMetrics.density).toInt()
            }
            background = ContextCompat.getDrawable(context, R.drawable.rounded_container_background)
            setPadding(
                (16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt()
            )
        }

        val circleDay = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.setMargins(0, 0, (128 * resources.displayMetrics.density).toInt(), 0)
                it.gravity = Gravity.CENTER_VERTICAL
            }
            textSize = 24f
            text = workout.day
            gravity = Gravity.CENTER
            setPadding(10, 10, 10, 10)
            setTextColor(Color.WHITE)
            background = ContextCompat.getDrawable(context, R.drawable.circle_shape)?.apply {
                (this as GradientDrawable).setColor(ContextCompat.getColor(context, R.color.circleDefColor))
            }
        }

        val workoutView = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.setMargins((75 * resources.displayMetrics.density).toInt(), 0, 0, 0)
                it.gravity = Gravity.CENTER_VERTICAL
            }
            textSize = 24f
            text = workout.name
            gravity = Gravity.CENTER_VERTICAL
        }

        dayContainer.addView(circleDay)
        dayContainer.addView(workoutView)

        if (workout.userId == currentUserId) {
            val deleteButton = ImageButton(context).apply {
                setImageResource(R.drawable.ic_delete) // Replace with your actual delete icon
                background = null
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).also {
                    it.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                }
                setOnClickListener {
                    viewModel.deleteWorkout(workout.docId)
                }
            }
            dayContainer.addView(deleteButton)
        }

        dayContainer.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToTrainingFragment(workout.name ?: "")
            navController.navigate(action)
        }

        binding.scheduleContainer.addView(dayContainer)
    }



    private fun showAddWorkoutDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_workout, null)
        val workoutNameEditText = dialogView.findViewById<EditText>(R.id.editTextWorkoutName)
        val daySpinner = dialogView.findViewById<Spinner>(R.id.spinnerDay)

        val daysArray = arrayOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, daysArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daySpinner.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Dodaj") { dialog, which ->
                val workoutName = workoutNameEditText.text.toString().trim()
                val selectedDay = daySpinner.selectedItem.toString()
                if (workoutName.isNotEmpty()) {
                    viewModel.addWorkoutToFirestore(workoutName, selectedDay) { docId ->
                        val workout = Workout(
                            name = workoutName,
                            day = selectedDay,
                            userId = viewModel.getCurrentUserId(), // You need to fetch the current user ID
                            docId = docId)
                        addScheduleToUI(workout)
                    }
                } else {
                    Toast.makeText(requireContext(), "Wprowadź nazwę treningu.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }












}