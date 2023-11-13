package com.example.pushpull

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.pushpull.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)


        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()




        // Tutaj dodajemy harmonogram treningów
        addScheduleToUI("Pn", "Klatka i Triceps", R.color.circleColorMo)
        addScheduleToUI("Wt", "Plecy, Brzuch i Biceps", R.color.circleColorWt)
        addScheduleToUI("Pt", "Nogi i Barki", R.color.circleColorPt)


        binding.exSearch.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_searchFragment)

        }



    }



    private fun addScheduleToUI(day: String, workout: String, colorRes: Int) {
        val context = context ?: return

        // Tworzenie kontenera dla dnia i treningu
        val dayContainer = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.bottomMargin = (12 * resources.displayMetrics.density).toInt() // Ustawianie marginesu dolnego
            }
            background = ContextCompat.getDrawable(context, R.drawable.rounded_container_background) // Ustawianie tła z zaokrąglonymi rogami
            // Ustaw padding dla kontenera, aby tekst nie dotykał bezpośrednio jego krawędzi
            setPadding((16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt())
        }

        // Tworzenie TextView dla dnia
        val circleDay = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.setMargins(0, 0, (128 * resources.displayMetrics.density).toInt(), 0) // Ustawianie marginesu prawego
                it.gravity = Gravity.CENTER_VERTICAL // Ustawienie wyrównania do środka w pionie
            }
            textSize = 24f
            text = day
            gravity = Gravity.CENTER
            setPadding(10, 10, 10, 10)
            setTextColor(Color.WHITE)
            // Pobieranie i ustawianie kształtu kółka
            background = ContextCompat.getDrawable(context, R.drawable.circle_shape)?.apply {
                (this as GradientDrawable).setColor(ContextCompat.getColor(context, colorRes))
            }
        }

        // Tworzenie TextView dla treningu
        val workoutView = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.setMargins((75 * resources.displayMetrics.density).toInt(), 0, 0, 0) // Ustawianie marginesu lewego
                it.gravity = Gravity.CENTER_VERTICAL // Ustawienie wyrównania do środka w pionie
            }
            textSize = 24f
            text = workout
            gravity = Gravity.CENTER_VERTICAL // Ustawienie wyrównania tekstu do środka w pionie
        }

        // Dodawanie widoków do kontenera
        dayContainer.addView(circleDay)
        dayContainer.addView(workoutView)

        // Dodaj OnClickListener do kontenera
        dayContainer.setOnClickListener {
            // Tu możesz przekazać argumenty do TrainingFragment, jeśli są potrzebne
            // na przykład nazwę treningu lub jakiś identyfikator
            val action = HomeFragmentDirections.actionHomeFragmentToTrainingFragment(workout)
            navController.navigate(action)
        }

        // Dodawanie kontenera do głównego kontenera LinearLayout w ScrollView
        binding.scheduleContainer.addView(dayContainer)
    }





}