package com.example.pushpull.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.pushpull.R
import com.example.pushpull.data.History
import com.example.pushpull.databinding.FragmentHistoryBinding
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
        Log.d("HistoryViewModel", "Updating UI with history entries count: ${historyEntries.size}")

        if (historyEntries.isEmpty()) {
            binding.tvEmptyHistory.visibility = View.VISIBLE
        } else {
            binding.tvEmptyHistory.visibility = View.GONE

            historyEntries.forEach { historyEntry ->
                addHistoryToUI(historyEntry)
            }
        }
    }

    private fun addHistoryToUI(historyEntry: History) {
        val context = requireContext()

        // Widoki dla dnia, nazwy, daty czasu
        val dayCircle = createDayCircle(context, historyEntry.day ?: "N/A")
        val nameView = createNameView(context, historyEntry.name ?: "Brak nazwy")
        val dateView =
            createTextView(context, formatDate(historyEntry.date), Gravity.CENTER_HORIZONTAL)
        val trainingTimeView = createTextView(
            context,
            formatTrainingTime(historyEntry.trainingTime),
            Gravity.CENTER_HORIZONTAL
        )


        val dayCircleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = (16 * resources.displayMetrics.density).toInt()
            gravity = Gravity.CENTER_HORIZONTAL
        }
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

        entryContainer.addView(dateView)
        entryContainer.addView(dayCircle, dayCircleParams)
        entryContainer.addView(nameView)
        entryContainer.addView(trainingTimeView)

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
                (this as GradientDrawable).setColor(
                    ContextCompat.getColor(
                        context,
                        R.color.circleDefColor
                    )
                )
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
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.gravity = Gravity.CENTER_HORIZONTAL
            }
            textSize = 24f
            text = nameText
            textAlignment = View.TEXT_ALIGNMENT_CENTER
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
