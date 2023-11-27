package com.example.pushpull.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pushpull.data.Exercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExerciseListViewModel : ViewModel() {
    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> get() = _exercises

    fun fetchExercisesForMuscleGroup(muscleGroup: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        db.collection("Exercises")
            .whereEqualTo("muscleGroup", muscleGroup)
            .get()
            .addOnSuccessListener { snapshot ->
                val exercisesList = snapshot.documents.mapNotNull { document ->
                    document.toObject(Exercise::class.java)?.apply {
                        docId = document.id
                    }
                }.filter {
                    it.userId == null || it.userId == userId
                }
                _exercises.value = exercisesList
            }

    }


}