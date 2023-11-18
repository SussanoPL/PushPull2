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

        // Fetch exercises
        db.collection("Exercises")
            .whereEqualTo("muscleGroup", muscleGroup)
            .get()
            .addOnSuccessListener { snapshot ->
                val exercisesList = snapshot.documents.mapNotNull { document ->
                    document.toObject(Exercise::class.java)?.apply {
                        docId = document.id // Set the document ID
                    }
                }.filter {
                    // Include only public exercises and those belonging to the user
                    it.userId == null || it.userId == userId
                }
                _exercises.value = exercisesList
            }

    }




}