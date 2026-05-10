package com.stayfinder.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stayfinder.app.databinding.ItemSearchChipBinding

class SearchHistoryAdapter(private val onChipClick: (String) -> Unit) :
    ListAdapter<String, SearchHistoryAdapter.SearchViewHolder>(StringDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemSearchChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SearchViewHolder(private val binding: ItemSearchChipBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(query: String) {
            binding.tvSearchQuery.text = query
            binding.root.setOnClickListener { onChipClick(query) }
        }
    }

    class StringDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}