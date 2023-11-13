package com.example.pushpull.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    // Funkcja do pobierania nazw ćwiczeń oraz ich szczegółów
    private fun fetchExerciseNames(exercisesList: List<Map<String, Any>>) {
        val exercisesWithIndex = mutableListOf<Pair<ExerciseData, Int>>()

        exercisesList.forEachIndexed { index, exerciseMap ->
            val exerciseId = exerciseMap["exerciseId"] as? String
            val repetitions = (exerciseMap["repetitions"] as? Number)?.toInt() ?: 0
            val sets = (exerciseMap["sets"] as? Number)?.toInt() ?: 0
            val weight = (exerciseMap["weight"] as? Number)?.toDouble() ?: 0.0

            exerciseId?.let { id ->
                // Rejestrujemy SnapshotListener dla każdego dokumentu ćwiczenia.
                val listener = db.collection("Exercises").document(id)
                    .addSnapshotListener { documentSnapshot, e ->
                        if (e != null) {
                            Log.e("TrainingViewModel", "Error fetching exercise details: ", e)
                            return@addSnapshotListener
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            val exerciseName = documentSnapshot.getString("name") ?: "Nieznane ćwiczenie"
                            // Tworzymy obiekt ExerciseData i dodajemy do listy wraz z indeksem.
                            val exerciseDetails = ExerciseData(exerciseName, repetitions, sets, weight)
                            exercisesWithIndex.add(Pair(exerciseDetails, index))
                            // Jeśli wszystkie ćwiczenia zostały pobrane, aktualizujemy LiveData.
                            if (exercisesWithIndex.size == exercisesList.size) {
                                val sortedExercises = exercisesWithIndex.sortedBy { it.second }
                                    .map { it.first }
                                _exerciseData.postValue(sortedExercises)
                            }
                        }
                    }
                // Dodajemy listener do listy, abyśmy mogli go usunąć w odpowiednim czasie.
                listeners.add(listener)
            }
        }
    }

   data class ExerciseData(
        val name: String,
        val repetitions: Int,
        val sets: Int,
        val weight: Double
    )




}