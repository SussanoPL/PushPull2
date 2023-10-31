package com.example.pushpull

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.pushpull.databinding.FragmentExerciseListBinding
import com.example.pushpull.databinding.FragmentSearchBinding
import com.example.pushpull.viewmodels.ExerciseListViewModel
import com.example.pushpull.viewmodels.SearchViewModel


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

    }

    private fun updateUIWithExercises(exercises: List<String>) {
        binding.exerciseListContainer.removeAllViews()

        for (exercise in exercises) {
            val textView = TextView(context)
            textView.text = exercise
            textView.textSize = 24f
            textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            textView.gravity = Gravity.CENTER
            textView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val margin = (8 * resources.displayMetrics.density).toInt()
                setMargins(margin, margin, margin, margin)
            }
            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button_background)

            textView.setOnClickListener {
                val navController = findNavController()
                val action = ExerciseListFragmentDirections.actionExerciseListFragmentToExerciseDetailsFragment(exercise)
                navController.navigate(action)
            }

            binding.exerciseListContainer.addView(textView)
        }
    }
}