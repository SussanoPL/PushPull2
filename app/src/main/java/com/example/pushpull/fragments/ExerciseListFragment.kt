package com.example.pushpull.fragments

import android.os.Bundle
import android.view.Gravity
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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pushpull.R
import com.example.pushpull.data.Exercise
import com.example.pushpull.databinding.DialogAddExerciseBinding
import com.example.pushpull.databinding.FragmentExerciseListBinding
import com.example.pushpull.viewmodels.ExerciseListViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ExerciseListFragment : Fragment() {

    private lateinit var binding: FragmentExerciseListBinding
    private val viewModel: ExerciseListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.rounded_button_background
                )
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

            exerciseLayout.setOnClickListener {
                val comingFromTrainingFragment =
                    arguments?.getBoolean("comingFromTrainingFragment") ?: false

                if (comingFromTrainingFragment) {
                    setFragmentResult("exerciseSelected", bundleOf("exerciseName" to exercise.name))
                    findNavController().popBackStack(R.id.trainingFragment, false)
                } else {
                    val action =
                        ExerciseListFragmentDirections.actionExerciseListFragmentToExerciseDetailsFragment(
                            exercise.name,
                            exercise.docId
                        )
                    findNavController().navigate(action)
                }
            }


            // Przycisk usuwania dla zalogowanego
            if (exercise.userId == currentUserId) {
                val deleteButton = ImageButton(context).apply {
                    setImageResource(R.drawable.ic_delete)
                    background = null
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                    setOnClickListener {
                        it.stopPropagation()
                        deleteExercise(exercise.docId)
                    }
                }
                exerciseLayout.addView(deleteButton)
            }

            binding.exerciseListContainer.addView(exerciseLayout)
        }
    }

    fun View.stopPropagation() {
        setOnClickListener { it ->
            it.stopPropagation()
        }
    }


    private fun showDialogToAddExercise() {
        val dialogBinding = DialogAddExerciseBinding.inflate(layoutInflater)

        val muscleGroups = arrayOf(
            "Wybierz grupę mięśniową",
            "Barki",
            "Biceps",
            "Brzuch",
            "Klatka piersiowa",
            "Plecy",
            "Pośladki",
            "Triceps",
            "Uda",
            "Łydki"
        ) // Include a prompt as the first item
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, muscleGroups)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerMuscleGroup.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Dodaj", null)
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = dialogBinding.editTextExerciseName.text.toString().trim()
            val muscleGroupPosition = dialogBinding.spinnerMuscleGroup.selectedItemPosition
            val muscleGroup = if (muscleGroupPosition > 0) muscleGroups[muscleGroupPosition] else ""
            val equipment = dialogBinding.editTextEquipment.text.toString().trim()
            val description = dialogBinding.editTextDescription.text.toString().trim()

            if (name.isNotEmpty() && muscleGroup.isNotEmpty()) {
                val exercise = Exercise(name, muscleGroup, equipment, description)
                addExerciseToFirestore(exercise)
                dialog.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Wszystkie pola muszą być wypełnione",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun addExerciseToFirestore(exercise: Exercise) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Użytkownik niezalogowany", Toast.LENGTH_SHORT).show()
            return
        }

        val exerciseWithUser = exercise.copy(userId = userId)

        val db = FirebaseFirestore.getInstance()
        // Dodaj nowe cwiczenie
        db.collection("Exercises")
            .add(exerciseWithUser)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(requireContext(), "Ćwiczenie dodane pomyślnie!", Toast.LENGTH_SHORT)
                    .show()
                fetchExercises() // Call a method to fetch and update the UI
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Error adding exercise: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
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
        val muscleGroup = arguments?.getString("muscleGroup") ?: return

        viewModel.fetchExercisesForMuscleGroup(muscleGroup)
    }


}