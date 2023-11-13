package com.example.pushpull.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ExerciseListViewModel : ViewModel() {

    private val _exercises = MutableLiveData<List<String>>()
    val exercises: LiveData<List<String>> get() = _exercises

    fun fetchExercisesForMuscleGroup(muscleGroup: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Exercises")
            .whereEqualTo("muscleGroup", muscleGroup)
            .addSnapshotListener { querySnapshot, _ ->
                val exercisesForGroup = querySnapshot?.documents
                    ?.mapNotNull { it.getString("name") }
                    ?.sorted() ?: emptyList()
                _exercises.value = exercisesForGroup
            }
    }


}