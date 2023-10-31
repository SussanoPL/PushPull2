package com.example.pushpull

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher

import android.view.Gravity
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.pushpull.databinding.FragmentSearchBinding
import com.example.pushpull.viewmodels.SearchViewModel


class SearchFragment() : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var navController: NavController
    private val viewModel: SearchViewModel by viewModels()






    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@SearchFragment.viewModel
        }

        binding.exSearch.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                v.clearFocus()
                val imm: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }




        viewModel.muscleGroups.observe(viewLifecycleOwner) { muscleGroups ->
            binding.muscleGroupContainer.removeAllViews()
            muscleGroups.sorted().forEach { addMuscleGroupToUI(it) }
        }

        viewModel.fetchMuscleGroups()

        binding.exSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Brak implementacji
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Brak implementacji
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isNotEmpty() == true) {
                    binding.exerciseScrollView.visibility = View.VISIBLE
                    binding.muscleScrollView.visibility = View.GONE
                    viewModel.searchExercises(s.toString())

                } else {
                    binding.exerciseScrollView.visibility = View.GONE
                    binding.muscleScrollView.visibility = View.VISIBLE
                }
            }
        })

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            binding.suggestionsContainer.removeAllViews()
            results.forEach { exerciseName ->
                val textView = TextView(context)
                textView.text = exerciseName
                textView.gravity = Gravity.CENTER
                textView.textSize = 24f
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val margin = (8 * resources.displayMetrics.density).toInt()
                params.setMargins(0, margin, 0, margin)


                textView.layoutParams = params
                textView.isClickable = true
                textView.isFocusable = true
                textView.setOnClickListener {
                    val navController = findNavController()
                    val action = SearchFragmentDirections.actionSearchFragmentToExerciseDetailsFragment(exerciseName)
                    navController.navigate(action)                }
                binding.suggestionsContainer.addView(textView)
            }
        }


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

        container.addView(button)
        cardView.isClickable = false
        button.isClickable = false
        button.isFocusable = false
        button.isFocusableInTouchMode = false



        binding.muscleGroupContainer.addView(container)

        container.setOnClickListener {
            navController = findNavController()
            val action = SearchFragmentDirections.actionSearchFragmentToExerciseListFragment(muscleGroup)
            navController.navigate(action)
        }
    }




}