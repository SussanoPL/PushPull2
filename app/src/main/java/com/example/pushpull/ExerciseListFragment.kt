package com.example.pushpull

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.pushpull.databinding.FragmentExerciseListBinding
import com.example.pushpull.databinding.FragmentSearchBinding
import com.example.pushpull.viewmodels.ExerciseListViewModel
import com.example.pushpull.data.Exercise
import com.example.pushpull.databinding.DialogAddExerciseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ExerciseListFragment : Fragment() {

    private lateinit var binding: FragmentExerciseListBinding
    private val viewModel: ExerciseListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExerciseListBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ExerciseListFragment.viewModel
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        val muscleGroup = arguments?.getString("muscleGroup") ?: ""

        (activity as? AppCompatActivity)?.supportActionBar?.title = muscleGroup

        viewModel.fetchExercisesForMuscleGroup(muscleGroup)

        viewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            updateUIWithExercises(exercises)
        }

        binding.fabAddExercise.setOnClickListener {
            showDialogToAddExercise()
        }

    }

    private fun updateUIWithExercises(exercises: List<Exercise>) {
        binding.exerciseListContainer.removeAllViews()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        exercises.forEach { exercise ->
            val exerciseLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    val margin = (8 * resources.displayMetrics.density).toInt()
                    setMargins(margin, margin, margin, margin)
                }
                gravity = Gravity.CENTER_VERTICAL
                background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button_background)
            }

            val textView = TextView(context).apply {
                text = exercise.name
                textSize = 24f
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            exerciseLayout.addView(textView)

            // Set click listener for the entire layout
            exerciseLayout.setOnClickListener {
                val navController = findNavController()
                val action = ExerciseListFragmentDirections.actionExerciseListFragmentToExerciseDetailsFragment(exercise.name, exercise.docId)
                navController.navigate(action)
            }

            // Add a delete button if the exercise belongs to the logged-in user
            if (exercise.userId == currentUserId) {
                val deleteButton = ImageButton(context).apply {
                    setImageResource(R.drawable.ic_delete) // Use your delete icon
                    background = null
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                    setOnClickListener {
                        // Prevent triggering the click listener for the entire layout when the button is clicked
                        it.stopPropagation()
                        deleteExercise(exercise.docId)
                    }
                }
                exerciseLayout.addView(deleteButton)
            }

            // Add the linear layout to your container
            binding.exerciseListContainer.addView(exerciseLayout)
        }
    }

    // Extension function to prevent click events from propagating to the parent
    fun View.stopPropagation() {
        setOnClickListener { it ->
            it.stopPropagation()
        }
    }



    private fun showDialogToAddExercise() {
        // Inflate the dialog with View Binding
        val dialogBinding = DialogAddExerciseBinding.inflate(layoutInflater)

        // Set up the spinner with the muscle groups
        val muscleGroups = arrayOf("Wybierz grupę mięśniową", "Barki", "Biceps", "Brzuch", "Klatka piersiowa", "Plecy", "Pośladki", "Triceps", "Uda", "Łydki") // Include a prompt as the first item
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, muscleGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerMuscleGroup.adapter = adapter

        // Create and show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Dodaj", null) // We will override the onClick later to prevent dialog from closing on errors
            .setNegativeButton("Anuluj", null)
            .create()

        // Show the dialog and capture the positive button here to override the onClick
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = dialogBinding.editTextExerciseName.text.toString().trim()
            val muscleGroupPosition = dialogBinding.spinnerMuscleGroup.selectedItemPosition
            val muscleGroup = if (muscleGroupPosition > 0) muscleGroups[muscleGroupPosition] else "" // Skip the prompt
            val equipment = dialogBinding.editTextEquipment.text.toString().trim()
            val description = dialogBinding.editTextDescription.text.toString().trim()

            if (name.isNotEmpty() && muscleGroup.isNotEmpty()) {
                val exercise = Exercise(name, muscleGroup, equipment, description)
                addExerciseToFirestore(exercise)
                dialog.dismiss() // Only dismiss the dialog if the input is valid
            } else {
                Toast.makeText(requireContext(), "Wszystkie pola muszą być wypełnione", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addExerciseToFirestore(exercise: Exercise) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Użytkownik niezalogowany", Toast.LENGTH_SHORT).show()
            return
        }

        // Include the userId in the exercise object or as a separate field
        val exerciseWithUser = exercise.copy(userId = userId) // Assuming Exercise has a userId field now

        val db = FirebaseFirestore.getInstance()
        // Add a new document to the 'Exercises' collection
        db.collection("Exercises")
            .add(exerciseWithUser)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(requireContext(), "Ćwiczenie dodane pomyślnie!", Toast.LENGTH_SHORT).show()
                fetchExercises() // Call a method to fetch and update the UI
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error adding exercise: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteExercise(docId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Exercises").document(docId).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Usunięto dodane ćwiczenie", Toast.LENGTH_SHORT).show()
                fetchExercises() // Call a method to fetch and update the UI
            }
            .addOnFailureListener {
                Toast.makeText(context, "Nie udało się usunąć ćwiczenia", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchExercises() {
        // Obtain muscle group from arguments or ViewModel
        val muscleGroup = arguments?.getString("muscleGroup") ?: return

        // Fetch exercises for this muscle group and update LiveData
        viewModel.fetchExercisesForMuscleGroup(muscleGroup)
    }




}