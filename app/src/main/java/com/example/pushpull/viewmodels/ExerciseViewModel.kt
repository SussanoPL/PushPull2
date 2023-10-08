package com.example.pushpull.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pushpull.data.Exercise

class ExerciseViewModel: ViewModel() {
    private val exercisesLiveData = MutableLiveData<List<Exercise>>()

    fun setExercises(exercises: List<Exercise>) {
        exercisesLiveData.value = exercises
    }

    fun getExercises(): LiveData<List<Exercise>> {
        return exercisesLiveData
    }

}