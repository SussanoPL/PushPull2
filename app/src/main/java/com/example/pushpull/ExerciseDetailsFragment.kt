package com.example.pushpull

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.pushpull.data.Exercise
import com.example.pushpull.databinding.DialogAddExerciseBinding
import com.example.pushpull.databinding.FragmentExerciseDetailsBinding
import com.example.pushpull.databinding.FragmentExerciseListBinding
import com.example.pushpull.viewmodels.ExerciseDetailsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ExerciseDetailsFragment : Fragment() {
    private lateinit var binding: FragmentExerciseDetailsBinding
    private val viewModel: ExerciseDetailsViewModel by viewModels()
    private var docId: String = ""



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding = FragmentExerciseDetailsBinding.inflate(inflater, container, false).apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = this@ExerciseDetailsFragment.viewModel
            }

            docId = arguments?.getString("docId") ?: ""
            if (docId.isBlank()) {
                Log.e("ExerciseDetailsFragment", "No docId passed to ExerciseDetailsFragment")
            }


            val exerciseName = arguments?.getString("name") ?: ""
            viewModel.fetchExerciseDetailsByName(exerciseName) // przekazujemy wartość do ViewModel

            viewModel.userId.observe(viewLifecycleOwner) { userId ->
                // Compare with the current logged-in user's ID
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                binding.fabEditExercise.visibility = if (userId == currentUserId) View.VISIBLE else View.GONE
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.fabEditExercise.setOnClickListener {
            // Pass the exercise details to the edit dialog or fragment
            val exercise = Exercise(
                name = viewModel.exerciseName.value ?: "",
                muscleGroup = viewModel.muscleGroup.value,
                equipment = viewModel.equipment.value,
                description = viewModel.description.value,
                userId = viewModel.userId.value,
                docId = docId

            )
            showEditExerciseDialog(exercise)
        }
    }

    private fun showEditExerciseDialog(exercise: Exercise) {
        val dialogBinding = DialogAddExerciseBinding.inflate(layoutInflater)

        // Initialize the spinner with the muscle groups
        val muscleGroups = arrayOf("Barki", "Biceps", "Brzuch", "Klatka piersiowa", "Plecy", "Pośladki", "Triceps", "Uda", "Łydki") // Include a prompt as the first item
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, muscleGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerMuscleGroup.adapter = adapter

        // Set the spinner to the current muscle group
        val muscleGroupPosition = muscleGroups.indexOf(exercise.muscleGroup)
        if (muscleGroupPosition != -1) {
            dialogBinding.spinnerMuscleGroup.setSelection(muscleGroupPosition)
        }

        // Pre-fill other exercise details
        dialogBinding.editTextExerciseName.setText(exercise.name)
        dialogBinding.editTextEquipment.setText(exercise.equipment)
        dialogBinding.editTextDescription.setText(exercise.description)

        // Create and show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Edytuj", null)
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            // Get the updated details from the dialog
            val updatedExercise = Exercise(
                name = dialogBinding.editTextExerciseName.text.toString().trim(),
                muscleGroup = dialogBinding.spinnerMuscleGroup.selectedItem.toString(),
                equipment = dialogBinding.editTextEquipment.text.toString().trim(),
                description = dialogBinding.editTextDescription.text.toString().trim(),
                userId = exercise.userId,  // Keep the original creator userId
                docId = exercise.docId  // Keep the document ID for updating
            )

            // Update the exercise in Firestore
            updateExercise(updatedExercise)
            dialog.dismiss()
        }
    }


    private fun updateExercise(exercise: Exercise) {
        val db = FirebaseFirestore.getInstance()
        exercise.docId.takeIf { it.isNotBlank() }?.let { docId ->
            db.collection("Exercises").document(docId)
                .set(exercise)
                .addOnSuccessListener {
                    Toast.makeText(context, "Ćwiczenie zostało zaktualizowane!", Toast.LENGTH_SHORT).show()
                    viewModel.fetchExerciseDetailsByName(exercise.name)  // Refresh the details
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Nie udało się zaktualizować ćwiczenia!", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

