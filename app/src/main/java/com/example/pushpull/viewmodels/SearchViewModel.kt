package com.example.pushpull.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pushpull.R
import com.google.firebase.firestore.FirebaseFirestore

class SearchViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _muscleGroups = MutableLiveData<List<String>>()
    val muscleGroups: LiveData<List<String>> get() = _muscleGroups
    val searchResults = MutableLiveData<List<String>>()


    fun fetchMuscleGroups() {
        db.collection("Exercises")
            .addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null) {
                    val muscleGroups = querySnapshot.documents
                        .mapNotNull { it.getString("muscleGroup") }
                        .distinct()
                    _muscleGroups.value = muscleGroups
                }
            }
    }

    fun searchExercises(query: String) {
        val capitalizedQuery = query.replaceFirstChar { it.uppercase() }

        db.collection("Exercises")
            .orderBy("name")
            .startAt(capitalizedQuery)
            .endAt(capitalizedQuery + "\uf8ff")
            .addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null) {
                    val exerciseNames = querySnapshot.documents
                        .map { it.getString("name") ?: "" }
                    searchResults.value = exerciseNames
                }
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