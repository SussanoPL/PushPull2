package com.example.pushpull.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pushpull.R
import com.google.firebase.firestore.FirebaseFirestore

class SearchViewModel:ViewModel() {
    private val _muscleGroups = MutableLiveData<List<String>>()
    val muscleGroups: LiveData<List<String>> get() = _muscleGroups

    fun fetchMuscleGroups() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Exercises")
            .get()
            .addOnSuccessListener { documents ->
                val muscleGroups = documents.mapNotNull { it.getString("muscleGroup") }.distinct()
                _muscleGroups.value = muscleGroups
            }
    }

    val muscleImageMap = mapOf(
        "Brzuch" to R.drawable.abdominals,
        "Plecy" to R.drawable.back,
        "Biceps" to R.drawable.biceps,
        "Łydki" to R.drawable.calves,
        "Klatka piersiowa" to R.drawable.chest,
        "Barki" to R.drawable.delts,
        "Pośladki" to R.drawable.glutes,
        "Uda" to R.drawable.thigh,
        "Triceps" to R.drawable.triceps
    )
}