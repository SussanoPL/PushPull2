package com.example.pushpull

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.adapters.ViewBindingAdapter.setPadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pushpull.databinding.DialogEditExerciseBinding
import com.example.pushpull.databinding.FragmentTrainingBinding
import com.example.pushpull.viewmodels.TrainingViewModel
import com.google.firebase.auth.FirebaseAuth


class TrainingFragment : Fragment() {
    private lateinit var binding: FragmentTrainingBinding
    private val viewModel: TrainingViewModel by viewModels()
    private var workoutId: String? = null
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var startTime: Long = 0

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }






    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrainingBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@TrainingFragment.viewModel
        }

        val workoutName = arguments?.getString("workout") ?: "Brak treningu o tej nazwie"
        viewModel.fetchWorkoutByName(workoutName)
        viewModel.fetchWorkoutIdByName(workoutName)

        setupObservers()




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabEditWorkout.setOnClickListener {
            // Only show edit options if the user is confirmed to own the workout
            val workoutId = workoutId
            if (workoutId != null && viewModel.ownWorkout.value == true) {
                showEditOptions(workoutId)
            } else {
                Toast.makeText(context, "Możesz tylko edytować własne ćwiczenia.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.editNotesImageView.setOnClickListener {
            viewModel.notes.value?.let { currentNotes ->
                showEditNotesDialog(currentNotes)
            }
        }

        //TIMER

        binding.fabStart.setOnClickListener {
            if (isTimerRunning) {
                // Stop the timer
                countDownTimer?.cancel()
                isTimerRunning = false
                binding.fabStart.setImageResource(R.drawable.ic_play) // Change icon to 'play'

                // Pass workoutId and startTime directly to the saveWorkoutToHistory function
                val workoutId = this.workoutId // Assuming this.workoutId is the ID you have in your fragment
                if (!workoutId.isNullOrEmpty()) {
                    viewModel.saveWorkoutToHistory(workoutId, startTime)
                }
            } else {
                // Timer is not running, so start the timer
                isTimerRunning = true
                binding.fabStart.setImageResource(R.drawable.ic_stop) // Change icon to 'stop'
                startTime = System.currentTimeMillis()
                countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) { // Long.MAX_VALUE acts as an "indefinite" timer
                    override fun onTick(millisUntilFinished: Long) {
                        val elapsed = System.currentTimeMillis() - startTime
                        binding.textViewTimer.text = formatTime(elapsed)
                    }

                    override fun onFinish() {
                        // This is not expected to be called in an "indefinite" timer scenario
                    }
                }.start()
            }
        }

        setFragmentResultListener("exerciseSelected") { _, bundle ->
            val selectedExerciseName = bundle.getString("exerciseName")
            // Update the ViewModel with the selected exercise name
            selectedExerciseName?.let {
                viewModel.setSelectedExerciseName(it)
                // Open the dialog with the selected exercise name
                workoutId?.let { id ->
                    showEditOptions(id, it)
                }
            }
        }
    }



    private fun setupObservers() {
        var canEditExercises: Boolean = false


        viewModel.workoutId.observe(viewLifecycleOwner) { id ->
            workoutId = id
            if (id.isNotEmpty()) {
                viewModel.checkIfUserOwnsWorkout(id)
            }
        }



        viewModel.ownWorkout.observe(viewLifecycleOwner) { ownsWorkout ->
            // Update the edit permission based on the ownership status
            canEditExercises = ownsWorkout

            // Update the visibility of the floating action button
            binding.fabEditWorkout.visibility = if (ownsWorkout) View.VISIBLE else View.GONE
            binding.editNotesImageView.visibility = if (ownsWorkout) View.VISIBLE else View.GONE


            // Now call updateExerciseDetails with the new value of canEditExercises
            viewModel.exerciseData.value?.let { exerciseData ->
                updateExerciseDetails(exerciseData, canEditExercises)
            }
        }

        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            binding.trainingDescription.text = notes
        }

        viewModel.exercises.observe(viewLifecycleOwner) { exerciseNames ->
            updateExerciseNames(exerciseNames)
        }

        viewModel.exerciseData.observe(viewLifecycleOwner) { exerciseData ->
            // Pass the current edit permission status to updateExerciseDetails
            updateExerciseDetails(exerciseData, canEditExercises)
        }
    }

    // Function to update the exercise names
    private fun updateExerciseNames(exerciseNames: List<String>) {
        binding.exercisesContainer.removeAllViews()

        exerciseNames.forEach { name ->
            val nameTextView = createExerciseTextView(name, 18f)
            binding.exercisesContainer.addView(nameTextView)
        }
    }

    // Function to update the exercise details

    private fun updateExerciseDetails(exerciseDetails: List<TrainingViewModel.ExerciseData>, canEdit: Boolean) {
        // We assume that the details include the name, so we clear the container first.
        binding.exercisesContainer.removeAllViews()



        exerciseDetails.forEach { detail ->
            val exerciseContainer = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_HORIZONTAL // This centers the children horizontally
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val exerciseLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL // Center the text and buttons horizontally
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // Weight of 1 to use up all space except for the delete button
                )
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            }

            val nameTextView = createExerciseTextView(detail.name, 16f)

            exerciseLayout.addView(nameTextView)


            val setsTextView = createDetailTextView("Serie: ${detail.sets}", 14f)
            exerciseLayout.addView(setsTextView)

            val repsTextView = createDetailTextView("Powtórzenia: ${detail.repetitions}", 14f)
            exerciseLayout.addView(repsTextView)
            val formattedWeight = if (detail.weight % 1.0 != 0.0) {
                String.format("%.1f", detail.weight)
            } else {
                String.format("%.0f", detail.weight)
            }
            val weightTextView = createDetailTextView("Waga: $formattedWeight kg", 14f)
            exerciseLayout.addView(weightTextView)

            // Add the exerciseLayout to the exerciseContainer
            exerciseContainer.addView(exerciseLayout)
            if (canEdit) {
                // Create and add the edit button only if the userId matches
                val editButton = ImageButton(context).apply {
                    setImageResource(R.drawable.ic_edit) // Use your edit icon resource
                    background = null
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also {
                        // Set top margin here
                        it.topMargin = dpToPx(12)
                    }
                    setOnClickListener {
                        // Show the edit dialog with the current exercise data
                        showEditDialog(detail)
                    }
                }
                exerciseContainer.addView(editButton)

                // Create and add the delete button only if the userId matches
                val deleteButton = ImageButton(context).apply {
                    setImageResource(R.drawable.ic_delete) // Use your delete icon resource
                    background = null
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also {
                        // Set top margin here
                        it.topMargin = dpToPx(12)
                    }
                    setOnClickListener {
                        workoutId?.let { nonNullWorkoutId ->
                            val exerciseToRemove = mapOf(
                                "exerciseId" to detail.id as Any,
                                "repetitions" to detail.repetitions as Any,
                                "sets" to detail.sets as Any,
                                "weight" to detail.weight as Any
                            )
                            viewModel.deleteExerciseFromWorkout(nonNullWorkoutId, exerciseToRemove)
                            // Directly remove the view for this exercise
                            removeExerciseView(exerciseContainer)
                        } ?: run {
                            Toast.makeText(context, "Error: Workout ID is null.", Toast.LENGTH_SHORT).show()
                            Log.e("TrainingFragment", "workoutId is null and cannot proceed with deletion.")
                        }
                    }
                }

                // Add the delete button to the exerciseContainer
                exerciseContainer.addView(deleteButton)
            }

            // Finally, add the exerciseContainer to the exercisesContainer
            binding.exercisesContainer.addView(exerciseContainer)
        }
    }


    private fun createExerciseTextView(exerciseName: String, textSize: Float): TextView {
        return TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.textSize = textSize
            text = exerciseName
            setPadding(dpToPx(6), dpToPx(8), dpToPx(8), dpToPx(8))
        }
    }

    private fun createDetailTextView(detail: String, textSize: Float): TextView {
        return TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.textSize = textSize
            text = detail
            setPadding(dpToPx(8), dpToPx(4), dpToPx(4), dpToPx(4))
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun showEditOptions(workoutId: String, selectedExerciseName: String? = null) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_exercise, null)
        val dialogBinding = DialogEditExerciseBinding.bind(dialogView)

        // Set the selected exercise name if it's not null
        selectedExerciseName?.let {
            dialogBinding.textViewExName.text = it
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Dodaj") { _, _ ->
                val repetitions = dialogBinding.editTextRepetitions.text.toString().toIntOrNull() ?: 0
                val sets = dialogBinding.editTextSets.text.toString().toIntOrNull() ?: 0
                val weight = dialogBinding.editTextWeight.text.toString().toDoubleOrNull() ?: 0.0

                // Use the provided exercise name or get it from the dialog if not provided
                val pickedExerciseName = selectedExerciseName ?: dialogBinding.textViewExName.text.toString()
                if (pickedExerciseName.isNotEmpty()) {
                    viewModel.addNewExerciseToWorkout(workoutId, pickedExerciseName, repetitions, sets, weight)
                } else {
                    Toast.makeText(context, "Wybierz najpierw ćwiczenie.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.show()

        dialogBinding.buttonPickExercise.setOnClickListener {
            val action = TrainingFragmentDirections.actionTrainingFragmentToSearchFragment(comingFromTrainingFragment = true)
            findNavController().navigate(action)
            dialog.dismiss()
        }
    }

    private fun showEditDialog(exerciseData: TrainingViewModel.ExerciseData) {
        // Inflate the dialog with View Binding
        val dialogBinding = DialogEditExerciseBinding.inflate(layoutInflater)

        // Pre-fill dialog with exercise data
        dialogBinding.textViewExName.text = exerciseData.name
        dialogBinding.editTextRepetitions.setText(exerciseData.repetitions.toString())
        dialogBinding.editTextSets.setText(exerciseData.sets.toString())
        dialogBinding.editTextWeight.setText(exerciseData.weight.toString())

        // Show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Edytuj") { _, _ ->
                // Collect data from dialog and update exercise in ViewModel
                val updatedRepetitions = dialogBinding.editTextRepetitions.text.toString().toIntOrNull() ?: 0
                val updatedSets = dialogBinding.editTextSets.text.toString().toIntOrNull() ?: 0
                val updatedWeight = dialogBinding.editTextWeight.text.toString().toDoubleOrNull() ?: 0.0

                val updatedExerciseData = TrainingViewModel.ExerciseData(
                    id = exerciseData.id,
                    name = exerciseData.name,
                    repetitions = updatedRepetitions,
                    sets = updatedSets,
                    weight = updatedWeight
                )

                // Call ViewModel to update exercise
                workoutId?.let { id ->
                    viewModel.updateExerciseInWorkout(id, updatedExerciseData)
                }
            }
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.show()
    }

    private fun removeExerciseView(exerciseContainer: LinearLayout) {
        binding.exercisesContainer.removeView(exerciseContainer)
    }

    private fun showEditNotesDialog(currentNotes: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_notes, null)
        val editTextWorkoutNotes = dialogView.findViewById<EditText>(R.id.editTextWorkoutNotes)
        editTextWorkoutNotes.setText(currentNotes)

        AlertDialog.Builder(requireContext())
            .setTitle("Edytuj opis treningu")
            .setView(dialogView)
            .setPositiveButton("Edytuj") { _, _ ->
                val updatedNotes = editTextWorkoutNotes.text.toString()
                saveNotesToFirebase(updatedNotes)
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun saveNotesToFirebase(notes: String) {
        workoutId?.let { id ->
            viewModel.updateWorkoutNotes(id, notes)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel() // Cancel the timer
    }






}


