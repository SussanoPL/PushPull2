package com.example.pushpull

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.pushpull.adapters.TipsAdapter
import com.example.pushpull.databinding.FragmentTipsBinding
import com.example.pushpull.viewmodels.TipsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Timestamp


class TipsFragment : Fragment() {
    private lateinit var binding: FragmentTipsBinding
    private val viewModel: TipsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTipsBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@TipsFragment.viewModel
        }

        val adapter = TipsAdapter(emptyList()) // Inicjalizuj adapter z pustą listą
        binding.tipsRecyclerView.adapter = adapter


        viewModel.tips.observe(viewLifecycleOwner, Observer { tipsList ->
            adapter.updateTips(tipsList)
        })

        return binding.root
    }


}