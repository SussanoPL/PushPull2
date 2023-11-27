package com.example.pushpull.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.pushpull.adapters.TipsAdapter
import com.example.pushpull.databinding.FragmentTipsBinding
import com.example.pushpull.viewmodels.TipsViewModel


class TipsFragment : Fragment() {
    private lateinit var binding: FragmentTipsBinding
    private val viewModel: TipsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTipsBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@TipsFragment.viewModel
        }

        val adapter = TipsAdapter(emptyList())
        binding.tipsRecyclerView.adapter = adapter


        viewModel.tips.observe(viewLifecycleOwner, Observer { tipsList ->
            adapter.updateTips(tipsList)
        })

        return binding.root
    }


}