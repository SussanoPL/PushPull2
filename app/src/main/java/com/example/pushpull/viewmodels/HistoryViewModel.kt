package com.example.pushpull.viewmodels

import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pushpull.data.History
import com.example.pushpull.databinding.FragmentHistoryBinding
import com.example.pushpull.databinding.FragmentTipsBinding
import com.google.firebase.firestore.FirebaseFirestore

class HistoryViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _historyEntries = MutableLiveData<List<History>>()
    val historyEntries: LiveData<List<History>> = _historyEntries

    fun fetchHistoryForUser(userId: String) {
        db.collection("History")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle the error
                    Log.e("HistoryViewModel", "Error fetching history", e)
                    _historyEntries.value = emptyList() // Set an empty list in case of error
                    return@addSnapshotListener
                }

                val historyList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(History::class.java)
                } ?: emptyList() // Provide an empty list if the snapshot is null

                _historyEntries.value = historyList
            }
    }


}