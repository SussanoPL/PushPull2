package com.example.pushpull.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pushpull.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class TipsViewModel(application: Application) : AndroidViewModel(application) {
    private val _tips = MutableLiveData<List<String>>()
    private val firestore = FirebaseFirestore.getInstance()
    val tips: LiveData<List<String>> get() = _tips

    // Pole do przechowywania ostatnio pobranych porad
    private var lastFetchedTips: List<String>? = null

    init {
        loadTipsFromFirestore()
    }


    private fun generateRandomTips(tips: List<String>): List<String> {
        val randomTips = tips.shuffled()
        return randomTips.take(10)
    }

    private fun loadTipsFromFirestore() {
        val docRef = firestore.collection("tipsCollection").document("tipsDocument")

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("TipsViewModel", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val tipsData = snapshot.toObject(TipsData::class.java)
                if (tipsData != null) {
                    // Sprawdzanie, czy nowe dane różnią się od ostatnio pobranych danych
                    if (lastFetchedTips == null || lastFetchedTips != tipsData.allTips) {
                        lastFetchedTips = tipsData.allTips
                        _tips.value = generateRandomTips(tipsData.allTips)
                    }
                }
            } else {
                Log.d("TipsViewModel", "Current data: null")
            }
        }
    }

    data class TipsData(val allTips: List<String> = listOf())
}