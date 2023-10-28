package com.example.pushpull.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pushpull.R
import com.google.gson.Gson

class TipsViewModel(application: Application) : AndroidViewModel(application) {
    private val _tips = MutableLiveData<List<String>>()
    val tips: LiveData<List<String>> get() = _tips

    init {
        _tips.value = generateRandomTips()
    }


    private fun generateRandomTips(): List<String> {
        val allTips = loadTipsFromJson()

        val randomTips = allTips.shuffled()
        return randomTips.take(10)
    }

    data class TipsData(val allTips: List<String>)


    private fun loadTipsFromJson(): List<String> {
        val inputStream = getApplication<Application>().resources.openRawResource(R.raw.tips)
        val json = inputStream.bufferedReader().use { it.readText() }

        val gson = Gson()
        val tipsData = gson.fromJson(json, TipsData::class.java)

        return tipsData.allTips
    }




}