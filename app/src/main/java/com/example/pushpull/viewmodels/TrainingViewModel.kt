package com.example.pushpull.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TrainingViewModel: ViewModel() {
    val workout: LiveData<String> = MutableLiveData<String>()
    // Metoda do ustawienia nazwy treningu
    fun setWorkoutName(workoutName: String) {
        (workout as MutableLiveData).value = workoutName
    }


}