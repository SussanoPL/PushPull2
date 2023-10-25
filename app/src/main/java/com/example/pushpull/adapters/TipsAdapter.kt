package com.example.pushpull.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.RecyclerView
import com.example.pushpull.R
import com.example.pushpull.viewmodels.TipsViewModel

class TipsAdapter(private val viewModel: TipsViewModel) : RecyclerView.Adapter<TipsAdapter.TipsViewHolder>() {
    class TipsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tipText: TextView = itemView.findViewById(R.id.tipText)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_tip, parent, false)
        return TipsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TipsViewHolder, position: Int) {
        val randomTip = viewModel.getRandomTip()
        holder.tipText.text = randomTip
    }

    override fun getItemCount(): Int {
        return 10
    }


}