package com.example.pushpull.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pushpull.data.History
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _historyEntries = MutableLiveData<List<History>>()
    val historyEntries: LiveData<List<History>> = _historyEntries

    fun fetchHistoryForUser(userId: String) {
        db.collection("History")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING) // Add this line to sort by the date field
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle the error
                    Log.e("HistoryViewModel", "Error fetching history", e)
                    _historyEntries.value = emptyList()
                    return@addSnapshotListener
                }

                val historyList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(History::class.java)
                } ?: emptyList()

                _historyEntries.value = historyList
            }
    }


}