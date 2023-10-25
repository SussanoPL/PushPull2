package com.example.pushpull.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TipsViewModel : ViewModel() {
    private val _tips = MutableLiveData<List<String>>()
    val tips: LiveData<List<String>> get() = _tips


    init {
        _tips.value = listOf(
            "Nie wszystkie ćwiczenia są dla Ciebie, jeśli jakieś ciężko idzie warto je zamienić, znajdzie się odpowiednie ;).",
            "Gdy trenujesz plecy warto użyć małpiego chwytu.",
            "Nie zapominaj o rozciąganiu i rozgrzewce przed trenigiem, są bardzo ważne.",
            "Pomiędzy treningami daj mięśniom odpocząć i odpocznij cały kolejny dzień aby mogły się zregenerować i odbudować.",
            "Nie porównuj się z innymi, ważny jest Twój progres.",
            "Pamiętaj, ćwiczenia wielostawowe są bardzo ważne na początek, bo angażują wiele grup mięśni.",
            "Korzystaj ze strefy wolnych ciężarów, pozwoli Ci to na lepszą kontrolę mięśniową i zwiększy jej świadomość.",
            "Sen jest bardzo ważnym aspektem trenowania, staraj się wysypiać minimum 8 godzin.",
            "Odżywianie stanowi ponad połowę sukcesu, staraj się dostarczać naturalnych produktów wysokobiałkowych.",
            "Jeśli Twoja siłownia posiada saunę to rozważ korzystanie z niej w dniach przerwy od treningu",
            "Gdy ćwiczenia idą Ci za łatwo, zwiększaj ciężar.")
    }

    fun getRandomTip(): String {
        val tipsList = _tips.value ?: emptyList()
        return if (tipsList.isNotEmpty()) {
            val randomIndex = (0 until tipsList.size).random()
            tipsList[randomIndex]
        } else {
            "Brak porad"
        }
    }


}