package com.example.pushpull

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.pushpull.databinding.FragmentSearchBinding
import com.example.pushpull.databinding.FragmentTipsBinding
import com.example.pushpull.viewmodels.SearchViewModel
import com.google.firebase.firestore.FirebaseFirestore


class SearchFragment() : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModels()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@SearchFragment.viewModel
        }

        viewModel.muscleGroups.observe(viewLifecycleOwner) { muscleGroups ->
            muscleGroups.sorted().forEach { addMuscleGroupToUI(it) }
        }

        viewModel.fetchMuscleGroups()

        return binding.root
    }




    @SuppressLint("ResourceType")
    private fun addMuscleGroupToUI(muscleGroup: String) {
        val container = LinearLayout(context)
        container.orientation = LinearLayout.HORIZONTAL
        val containerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        containerParams.bottomMargin = (8 * resources.displayMetrics.density).toInt() // Dodaj margines do dolnej części, np. 16dp
        container.layoutParams = containerParams
        container.gravity = Gravity.CENTER_VERTICAL // Dodane wyrównanie

        container.setBackgroundResource(R.drawable.card_background) // Ustaw tło kontenera na card_background.xml


        val cardView = LayoutInflater.from(context).inflate(R.drawable.rounded_image, container, false) as CardView
        val imageViewInsideCard = cardView.findViewById<ImageView>(R.id.imageInsideCard)

        val imageResId = viewModel.muscleImageMap[muscleGroup]
        if (imageResId != null) {
            imageViewInsideCard.setImageResource(imageResId)
        }
        imageViewInsideCard.isClickable = true // Ustawiamy, że obrazek jest klikalny
        imageViewInsideCard.setOnClickListener {
            // Akcja po kliknięciu w obrazek
            Toast.makeText(context, "Kliknięto: $muscleGroup", Toast.LENGTH_SHORT).show()
        }

        container.addView(cardView)

        val button = Button(context)
        button.text = muscleGroup
        button.textSize = 24f
        button.background = ContextCompat.getDrawable(
            requireContext(),
            android.R.color.transparent
        )
        val buttonParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonParams.weight = 1f
        button.layoutParams = buttonParams

        button.setOnClickListener {
            Toast.makeText(context, "Kliknięto: $muscleGroup", Toast.LENGTH_SHORT).show()
        }
        container.addView(button)

        binding.muscleGroupContainer.addView(container)
    }
}