package com.example.pushpull

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.pushpull.adapters.TipsAdapter
import com.example.pushpull.databinding.FragmentTipsBinding
import com.example.pushpull.viewmodels.TipsViewModel

class TipsFragment : Fragment() {
    private lateinit var binding: FragmentTipsBinding
    private val viewModel: TipsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTipsBinding.inflate(inflater, container, false)
        val view = binding.root

        val adapter = TipsAdapter(viewModel.tips.value.orEmpty())
        binding.tipsRecyclerView.adapter = adapter


        return view
    }
}