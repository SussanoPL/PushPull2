package com.example.pushpull

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.pushpull.adapters.TipsAdapter
import com.example.pushpull.viewmodels.TipsViewModel


class TipsFragment : Fragment() {
    private val viewModel: TipsViewModel by viewModels()
    private lateinit var tipsRecyclerView: RecyclerView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_tips, container, false)

        tipsRecyclerView = view.findViewById(R.id.tipsRecyclerView)
        val adapter = TipsAdapter(emptyList()) // Możesz początkowo użyć pustej listy
        tipsRecyclerView.adapter = adapter

        // Obserwowanie danych ViewModel i aktualizowanie adaptera
        viewModel.tips.observe(viewLifecycleOwner, { tips ->
            adapter.updateTips(tips)
        })

        return view
    }
}