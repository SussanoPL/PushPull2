package com.example.pushpull.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class TrainingViewModel : ViewModel() {
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
                            val exerciseName =
                                documentSnapshot.getString("name") ?: "Unknown exercise"
                            val exerciseDetails =
                                ExerciseData(id, exerciseName, repetitions, sets, weight)
                            exercisesWithIndex.add(Pair(exerciseDetails, index))
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

    fun addExerciseToWorkout(workoutId: String, exerciseData: ExerciseData) {
        db.collection("Exercises")
            .whereEqualTo("name", exerciseData.name)
            .limit(1) // Assuming exercise names are unique
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    val exerciseDocument = documents.documents[0]
                    val exerciseId = exerciseDocument.id

                    val exerciseMap = mapOf(
                        "exerciseId" to exerciseId,
                        "repetitions" to exerciseData.repetitions,
                        "sets" to exerciseData.sets,
                        "weight" to exerciseData.weight
                    )

                    db.collection("Workouts").document(workoutId)
                        .update("exercises", FieldValue.arrayUnion(exerciseMap))
                        .addOnSuccessListener {
                            Log.d("TrainingViewModel", "Exercise added successfully to workout")
                        }
                        .addOnFailureListener { e ->
                            Log.e("TrainingViewModel", "Error adding exercise to workout", e)
                        }
                } else {
                    Log.e("TrainingViewModel", "No exercise found with that name")
                }
            }
            .addOnFailureListener { e ->
                Log.e("TrainingViewModel", "Error finding exercise by name", e)
            }
    }

    fun addNewExerciseToWorkout(
        workoutId: String,
        exerciseName: String,
        repetitions: Int,
        sets: Int,
        weight: Double
    ) {
        db.collection("Exercises")
            .whereEqualTo("name", exerciseName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    val exerciseId = documents.documents[0].id
                    addExerciseToWorkout(
                        workoutId, ExerciseData(
                            id = exerciseId,
                            name = exerciseName,
                            repetitions = repetitions,
                            sets = sets,
                            weight = weight
                        )
                    )
                } else {
                    Log.e("TrainingViewModel", "No exercise found with name: $exerciseName")
                }
            }
            .addOnFailureListener { e ->
                Log.e("TrainingViewModel", "Error finding exercise by name", e)
            }
    }

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

    fun updateExerciseInWorkout(workoutId: String, exerciseData: ExerciseData) {
        val updatedExerciseMap = mapOf(
            "exerciseId" to exerciseData.id,
            "repetitions" to exerciseData.repetitions,
            "sets" to exerciseData.sets,
            "weight" to exerciseData.weight
        )

        db.collection("Workouts").document(workoutId).get()
            .addOnSuccessListener { workoutDocument ->
                if (workoutDocument.exists()) {
                    val exercises =
                        workoutDocument.data?.get("exercises") as? List<Map<String, Any>>
                            ?: listOf()
                    val exerciseToUpdate = exercises.find { it["exerciseId"] == exerciseData.id }

                    if (exerciseToUpdate != null) {
                        db.collection("Workouts").document(workoutId)
                            .update("exercises", FieldValue.arrayRemove(exerciseToUpdate))
                            .addOnSuccessListener {
                                // Now add the updated exercise map using FieldValue.arrayUnion
                                db.collection("Workouts").document(workoutId)
                                    .update("exercises", FieldValue.arrayUnion(updatedExerciseMap))
                                    .addOnSuccessListener {
                                        Log.d(
                                            "TrainingViewModel",
                                            "Exercise updated successfully in workout"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                            "TrainingViewModel",
                                            "Error adding updated exercise to workout",
                                            e
                                        )
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e(
                                    "TrainingViewModel",
                                    "Error removing old exercise from workout",
                                    e
                                )
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
        val id: String?,
        val name: String,
        val repetitions: Int,
        val sets: Int,
        val weight: Double
    )

    fun setSelectedExerciseName(name: String) {
        _selectedExerciseName.value = name
    }

    fun saveWorkoutToHistory(workoutId: String, startTime: Long) {
        db.collection("Workouts").document(workoutId).get()
            .addOnSuccessListener { workoutDocument ->
                if (workoutDocument.exists()) {
                    val workoutData = workoutDocument.data

                    val trainingTime = System.currentTimeMillis() - startTime

                    val currentDate = FieldValue.serverTimestamp()

                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "null"

                    val historyData = workoutData?.toMutableMap().apply {
                        this?.put("trainingTime", trainingTime)
                        this?.put("date", currentDate)
                        this?.put("userId", userId)
                    }

                    historyData?.let {
                        db.collection("History").add(it)
                            .addOnSuccessListener {
                                Log.d("TrainingViewModel", "Workout saved successfully to History")
                            }
                            .addOnFailureListener { e ->
                                Log.e("TrainingViewModel", "Error saving workout to History", e)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TrainingViewModel", "Error fetching workout document", e)
            }
    }


}