package com.example.pushpull

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.pushpull.databinding.FragmentExerciseDetailsBinding
import com.example.pushpull.databinding.FragmentExerciseListBinding
import com.example.pushpull.viewmodels.ExerciseDetailsViewModel


class ExerciseDetailsFragment : Fragment() {
    private lateinit var binding: FragmentExerciseDetailsBinding
    private val viewModel: ExerciseDetailsViewModel by viewModels()



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding = FragmentExerciseDetailsBinding.inflate(inflater, container, false).apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = this@ExerciseDetailsFragment.viewModel
            }

            val exerciseName = arguments?.getString("name") ?: ""
            viewModel.fetchExerciseDetailsByName(exerciseName) // przekazujemy wartość do ViewModel


        return binding.root
    }
}