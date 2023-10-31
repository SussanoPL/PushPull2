package com.example.pushpull.viewmodels

import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ExerciseDetailsViewModel: ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _exerciseName = MutableLiveData<String>()
    val exerciseName: LiveData<String> get() = _exerciseName

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> get() = _description

    private val _equipment = MutableLiveData<String>()
    val equipment: LiveData<String> get() = _equipment

    private val _muscleGroup = MutableLiveData<String>()
    val muscleGroup: LiveData<String> get() = _muscleGroup

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchExerciseDetailsByName(exerciseName: String) {
        if (exerciseName.isNotEmpty()) {
            db.collection("Exercises")
                .whereEqualTo("name", exerciseName)
                .limit(1)  // Assuming exercise names are unique
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val document = querySnapshot.documents.firstOrNull()
                    if (document != null) {
                        _exerciseName.value = document.getString("name")
                        _description.value = document.getString("description")
                        _equipment.value = document.getString("equipment")
                        _muscleGroup.value = document.getString("muscleGroup")
                    } else {
                        _errorMessage.value = "No exercise found with the given name."
                    }
                }
                .addOnFailureListener { exception ->
                    _errorMessage.value = exception.localizedMessage ?: "An error occurred while fetching data."
                }
        } else {
            _errorMessage.value = "Invalid exercise name."
        }
    }
}