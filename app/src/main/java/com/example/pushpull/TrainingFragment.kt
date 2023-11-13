package com.example.pushpull

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.adapters.ViewBindingAdapter.setPadding
import androidx.fragment.app.viewModels
import com.example.pushpull.databinding.FragmentTrainingBinding
import com.example.pushpull.viewmodels.TrainingViewModel


class TrainingFragment : Fragment() {
    private lateinit var binding: FragmentTrainingBinding
    private val viewModel: TrainingViewModel by viewModels()

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

        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            binding.trainingDescription.text = notes
        }

        viewModel.exercises.observe(viewLifecycleOwner) { exerciseNames ->
            updateExerciseNames(exerciseNames)
        }

        viewModel.exerciseData.observe(viewLifecycleOwner) { exerciseData ->
            updateExerciseDetails(exerciseData)
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
    private fun updateExerciseDetails(exerciseDetails: List<TrainingViewModel.ExerciseData>) {
        // We assume that the details include the name, so we clear the container first.
        binding.exercisesContainer.removeAllViews()

        exerciseDetails.forEach { detail ->
            val exerciseLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            }

            val nameTextView = createExerciseTextView(detail.name, 18f)
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

            binding.exercisesContainer.addView(exerciseLayout)
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
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
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
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}

