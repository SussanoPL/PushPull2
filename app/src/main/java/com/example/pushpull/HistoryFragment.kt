package com.example.pushpull

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.adapters.ViewBindingAdapter.setPadding
import androidx.fragment.app.viewModels
import com.example.pushpull.data.History
import com.example.pushpull.databinding.FragmentHistoryBinding
import com.example.pushpull.databinding.FragmentHomeBinding
import com.example.pushpull.databinding.FragmentTipsBinding
import com.example.pushpull.viewmodels.HistoryViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            viewModel.fetchHistoryForUser(it.uid)
        }

        viewModel.historyEntries.observe(viewLifecycleOwner) { historyEntries ->
            updateUIWithHistoryData(historyEntries)
        }
    }

    private fun updateUIWithHistoryData(historyEntries: List<History>) {
        if (historyEntries.isEmpty()) {
            // Show the empty history message
            binding.tvEmptyHistory.visibility = View.VISIBLE
        } else {
            // Hide the empty history message
            binding.tvEmptyHistory.visibility = View.GONE

            // Add new views for each history entry
            historyEntries.forEach { historyEntry ->
                addHistoryToUI(historyEntry)
            }
        }
    }

    private fun addHistoryToUI(historyEntry: History) {
        val context = requireContext()

        // Create views for day, name, date, and training time
        val dayCircle = createDayCircle(context, historyEntry.day ?: "N/A")
        val nameView = createNameView(context, historyEntry.name ?: "Brak nazwy")
        val dateView = createTextView(context, formatDate(historyEntry.date), Gravity.CENTER_HORIZONTAL)
        val trainingTimeView = createTextView(context, formatTrainingTime(historyEntry.trainingTime), Gravity.CENTER_HORIZONTAL)


        // Set layout parameters with top margin for the dayCircle
        val dayCircleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = (16 * resources.displayMetrics.density).toInt() // Set top margin here, for example 16dp
            gravity = Gravity.CENTER_HORIZONTAL // If you want to center it horizontally
        }
        // Create a container for the day circle and the name.
        val entryContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.bottomMargin = (12 * resources.displayMetrics.density).toInt()
            }
            background = ContextCompat.getDrawable(context, R.drawable.rounded_container_background)
            setPadding(
                (16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt()
            )
        }

        // Add the date view above the day circle and name view.
        entryContainer.addView(dateView)
        entryContainer.addView(dayCircle, dayCircleParams)
        entryContainer.addView(nameView)
        entryContainer.addView(trainingTimeView)

        // Finally, add the entry container to the parent layout.
        binding.historyContainer.addView(entryContainer)
    }


    private fun createDayCircle(context: Context, dayText: String): TextView {
        return TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.setMargins(0, 0, (128 * resources.displayMetrics.density).toInt(), 0)
                it.gravity = Gravity.CENTER_VERTICAL
            }
            textSize = 24f
            text = dayText
            gravity = Gravity.CENTER
            setPadding(10, 10, 10, 10)
            setTextColor(Color.WHITE)
            background = ContextCompat.getDrawable(context, R.drawable.circle_shape)?.apply {
                (this as GradientDrawable).setColor(ContextCompat.getColor(context, R.color.circleDefColor))
            }
        }
    }

    private fun createTextView(context: Context, text: String, gravity: Int): TextView {
        return TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.gravity = gravity
            }
            this.gravity = gravity
            this.text = text
        }
    }

    private fun createNameView(context: Context, nameText: String): TextView {
        return TextView(context).apply {
            // Use LinearLayout.LayoutParams instead of FrameLayout.LayoutParams to center in LinearLayout
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also {
                // Set gravity to center horizontal if you want to center the text view within its container
                it.gravity = Gravity.CENTER_HORIZONTAL
            }
            textSize = 24f
            text = nameText
            // Set text alignment to center
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            // Set gravity to center vertical to center the text within the TextView
            gravity = Gravity.CENTER_VERTICAL
        }
    }


    private fun formatDate(date: Date?): String {
        val formatPattern = "d MMMM yyyy"
        val locale = Locale("pl", "PL")
        val dateFormatter = SimpleDateFormat(formatPattern, locale)
        return date?.let { dateFormatter.format(it) } ?: "Brak daty"
    }

    private fun formatTrainingTime(trainingTimeMillis: Long?): String {
        val minutes = (trainingTimeMillis ?: 0) / 60000
        return "$minutes min"
    }
}
