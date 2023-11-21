package com.example.pushpull.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


// Data class for Exercise Details.


class TrainingViewModel: ViewModel() {
    private val _notes = MutableLiveData<String>()
    val notes: LiveData<String> = _notes
    private val _exercises = MutableLiveData<List<String>>()
    val exercises: LiveData<List<String>> = _exercises
    private val _exerciseData = MutableLiveData<List<ExerciseData>>()
    val exerciseData: LiveData<List<ExerciseData>> = _exerciseData

    private val currentUser = FirebaseAuth.getInstance().currentUser

    private val _ownWorkout = MutableLiveData<Boolean>()
    val ownWorkout: LiveData<Boolean> get() = _ownWorkout
    // Add this declaration
    private val _workoutId = MutableLiveData<String>()
    val workoutId: LiveData<String> = _workoutId

    private val _selectedExerciseName = MutableLiveData<String>()
    val selectedExerciseName: LiveData<String> = _selectedExerciseName


    private val listeners = mutableListOf<ListenerRegistration>()
    // Referencja do bazy danych Firestore
    private val db = FirebaseFirestore.getInstance()

    // Funkcja pobierająca dane treningu na podstawie nazwy
    fun fetchWorkoutByName(workoutName: String) {

        val registration = db.collection("Workouts")
            .whereEqualTo("name", workoutName)
            .limit(1)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("TrainingViewModel", "Listen failed.", e)
                    _notes.value = "Wystąpił błąd podczas pobierania danych treningu."
                    return@addSnapshotListener
                }


                if (snapshot != null && snapshot.documents.isNotEmpty()) {
                    val workoutData = snapshot.documents[0].data
                    val notesValue = workoutData?.get("notes") as? String
                    _notes.value = notesValue ?: "Brak notatek dla tego treningu."

                    // Pobieranie listy ćwiczeń z treningu
                    val workoutExercises = workoutData?.get("exercises") as? List<Map<String, Any>>
                    workoutExercises?.let { exercisesList ->
                        fetchExerciseNames(exercisesList)
                    }
                } else {
                    _notes.value = "Nie znaleziono treningu o nazwie: $workoutName"
                }
            }
        listeners.add(registration)

    }

    fun fetchWorkoutIdByName(workoutName: String) {
        // Fetch the workout ID using the workout name
        db.collection("Workouts")
            .whereEqualTo("name", workoutName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    val workoutId = documents.documents[0].id
                    _workoutId.value = workoutId
                    checkIfUserOwnsWorkout(workoutId)
                }
            }
    }

    // Funkcja do pobierania nazw ćwiczeń oraz ich szczegółów
    private fun fetchExerciseNames(exercisesList: List<Map<String, Any>>) {
        val exercisesWithIndex = mutableListOf<Pair<ExerciseData, Int>>()

        exercisesList.forEachIndexed { index, exerciseMap ->
            val exerciseId = exerciseMap["exerciseId"] as? String
            val repetitions = (exerciseMap["repetitions"] as? Number)?.toInt() ?: 0
            val sets = (exerciseMap["sets"] as? Number)?.toInt() ?: 0
            val weight = (exerciseMap["weight"] as? Number)?.toDouble() ?: 0.0

            exerciseId?.let { id ->
                val listener = db.collection("Exercises").document(id)
                    .addSnapshotListener { documentSnapshot, e ->
                        if (e != null) {
                            Log.e("TrainingViewModel", "Error fetching exercise details: ", e)
                            return@addSnapshotListener
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            val exerciseName = documentSnapshot.getString("name") ?: "Unknown exercise"
                            // Here we use the document ID and the exercise name
                            val exerciseDetails = ExerciseData(id, exerciseName, repetitions, sets, weight)
                            exercisesWithIndex.add(Pair(exerciseDetails, index))
                            // If all exercises have been fetched, update LiveData
                            if (exercisesWithIndex.size == exercisesList.size) {
                                val sortedExercises = exercisesWithIndex.sortedBy { it.second }
                                    .map { it.first }
                                _exerciseData.postValue(sortedExercises)
                            }
                        }
                    }
                listeners.add(listener)
            }
        }
    }


    fun checkIfUserOwnsWorkout(workoutId: String) {
        currentUser?.uid?.let { userId ->
            db.collection("Workouts").document(workoutId).get()
                .addOnSuccessListener { document ->
                    val workoutUserId = document.getString("userId")
                    _ownWorkout.value = userId == workoutUserId
                }
                .addOnFailureListener {
                    // Handle any errors here
                }
        } ?: run {
            _ownWorkout.value = false
        }
    }

    fun updateWorkoutNotes(workoutId: String, notes: String) {
        db.collection("Workouts").document(workoutId)
            .update("notes", notes)
            .addOnSuccessListener {
                Log.d("TrainingViewModel", "Workout notes updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("TrainingViewModel", "Error updating workout notes", e)
            }
    }

    fun addExerciseToWorkout(workoutId: String, exerciseData: TrainingViewModel.ExerciseData) {
        // First, find the exercise ID by the exercise name
        db.collection("Exercises")
            .whereEqualTo("name", exerciseData.name)
            .limit(1) // Assuming exercise names are unique
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    val exerciseDocument = documents.documents[0]
                    val exerciseId = exerciseDocument.id

                    // Now that we have the exercise ID, we can create the map to add to the workout
                    val exerciseMap = mapOf(
                        "exerciseId" to exerciseId,
                        "repetitions" to exerciseData.repetitions,
                        "sets" to exerciseData.sets,
                        "weight" to exerciseData.weight
                    )

                    // Then, update the workout document with this exercise
                    db.collection("Workouts").document(workoutId)
                        .update("exercises", FieldValue.arrayUnion(exerciseMap))
                        .addOnSuccessListener {
                            Log.d("TrainingViewModel", "Exercise added successfully to workout")
                            // If needed, perform any additional actions like updating UI
                        }
                        .addOnFailureListener { e ->
                            Log.e("TrainingViewModel", "Error adding exercise to workout", e)
                        }
                } else {
                    // Handle the case where the exercise was not found by name
                    Log.e("TrainingViewModel", "No exercise found with that name")
                }
            }
            .addOnFailureListener { e ->
                Log.e("TrainingViewModel", "Error finding exercise by name", e)
            }
    }

    fun addNewExerciseToWorkout(workoutId: String, exerciseName: String, repetitions: Int, sets: Int, weight: Double) {
        // Fetch the ID for the exerciseName from Firestore
        db.collection("Exercises")
            .whereEqualTo("name", exerciseName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    val exerciseId = documents.documents[0].id
                    addExerciseToWorkout(workoutId, ExerciseData(
                        id = exerciseId,
                        name = exerciseName,
                        repetitions = repetitions,
                        sets = sets,
                        weight = weight
                    ))
                } else {
                    Log.e("TrainingViewModel", "No exercise found with name: $exerciseName")
                }
            }
            .addOnFailureListener { e ->
                Log.e("TrainingViewModel", "Error finding exercise by name", e)
            }
    }

    // Add a function to delete an exercise from the workout.
    fun deleteExerciseFromWorkout(workoutId: String, exercise: Map<String, Any>) {
        db.collection("Workouts").document(workoutId)
            .update("exercises", FieldValue.arrayRemove(exercise))
            .addOnSuccessListener {
                Log.d("TrainingViewModel", "Exercise removed successfully from workout")
                // Perform any additional UI updates here if necessary.
            }
            .addOnFailureListener { e ->
                Log.e("TrainingViewModel", "Error removing exercise from workout", e)
            }
    }

    fun updateExerciseInWorkout(workoutId: String, exerciseData: TrainingViewModel.ExerciseData) {
        // Assuming you have the exercise ID and the details are already correct.
        val updatedExerciseMap = mapOf(
            "exerciseId" to exerciseData.id, // Use the actual exerciseId, assuming it's part of ExerciseData
            "repetitions" to exerciseData.repetitions,
            "sets" to exerciseData.sets,
            "weight" to exerciseData.weight
        )

        // Fetch the current workout to get the existing exercises
        db.collection("Workouts").document(workoutId).get()
            .addOnSuccessListener { workoutDocument ->
                if (workoutDocument.exists()) {
                    val exercises = workoutDocument.data?.get("exercises") as? List<Map<String, Any>> ?: listOf()
                    // Find the exercise to be updated within the array
                    val exerciseToUpdate = exercises.find { it["exerciseId"] == exerciseData.id }

                    if (exerciseToUpdate != null) {
                        // Use FieldValue.arrayRemove to remove the old exercise map
                        db.collection("Workouts").document(workoutId)
                            .update("exercises", FieldValue.arrayRemove(exerciseToUpdate))
                            .addOnSuccessListener {
                                // Now add the updated exercise map using FieldValue.arrayUnion
                                db.collection("Workouts").document(workoutId)
                                    .update("exercises", FieldValue.arrayUnion(updatedExerciseMap))
                                    .addOnSuccessListener {
                                        Log.d("TrainingViewModel", "Exercise updated successfully in workout")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("TrainingViewModel", "Error adding updated exercise to workout", e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e("TrainingViewModel", "Error removing old exercise from workout", e)
                            }
                    } else {
                        Log.e("TrainingViewModel", "Exercise to update not found in workout")
                    }
                } else {
                    Log.e("TrainingViewModel", "Workout document does not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.e("TrainingViewModel", "Error fetching workout document", e)
            }
    }




    data class ExerciseData(
        val id: String?, // Can be null when creating a new exercise
        val name: String,
        val repetitions: Int,
        val sets: Int,
        val weight: Double
    )

    fun setSelectedExerciseName(name: String) {
        _selectedExerciseName.value = name
    }




}