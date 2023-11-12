package com.example.pushpull

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        // Odbieranie nazwy treningu przekazanej jako argument
        val workoutName = arguments?.getString("workout")

        // Ustawienie tekstu TextView bezpośrednio
        workoutName?.let {
            binding.trainingName.text = it
        }

        return binding.root
    }


}