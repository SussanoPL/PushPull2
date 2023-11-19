package com.example.pushpull.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pushpull.data.Exercise
import com.example.pushpull.data.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val workouts: LiveData<List<Workout>> get() = _workouts
    val errorMessage = MutableLiveData<String>()
    private val _workouts = MutableLiveData<List<Workout>>()

    fun fetchWorkouts() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Fetch workouts
        db.collection("Workouts")
            .get()
            .addOnSuccessListener { snapshot ->
                val workoutsList = snapshot.documents.mapNotNull { document ->
                    document.toObject(Workout::class.java)?.apply {
                        docId = document.id // Set the document ID
                    }
                }.filter {
                    // Include only public workouts and those belonging to the user
                    it.userId == null || it.userId == userId
                }.sortedBy { workout ->

                    dayOrder[workout.day]
                }
                _workouts.value = workoutsList
            }
    }




    fun addWorkoutToFirestore(workoutName: String, day: String, callback: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val workout = Workout(
            name = workoutName,
            day = day,
            userId = userId
        )
        db.collection("Workouts")
            .add(workout)
            .addOnSuccessListener { documentReference ->
                callback(documentReference.id) // Invoke the callback with the new document ID
                fetchWorkouts() // Refresh the workouts list
            }
            .addOnFailureListener { e ->
                errorMessage.value = "Error adding workout: ${e.localizedMessage}"
            }
    }
    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }



    fun deleteWorkout(docId: String) {
        if (docId.isBlank()) {
            errorMessage.value = "Invalid document ID"
            return
        }

        db.collection("Workouts").document(docId)
            .delete()
            .addOnSuccessListener {
                fetchWorkouts() // Refresh the workouts list
            }
            .addOnFailureListener { e ->
                errorMessage.value = "Error deleting workout: ${e.localizedMessage}"
            }
    }
    // Assume each workout has a 'day' property which is a string like "Pn", "Wt", etc.
    companion object {
        val dayOrder = mapOf(
            "Pn" to 1,
            "Wt" to 2,
            "Śr" to 3,
            "Cz" to 4,
            "Pt" to 5,
            "Sb" to 6,
            "Nd" to 7
        )
    }

}
