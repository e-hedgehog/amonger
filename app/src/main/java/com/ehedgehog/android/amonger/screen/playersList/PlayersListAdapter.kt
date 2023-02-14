package com.ehedgehog.android.amonger.screen.playersList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ehedgehog.android.amonger.screen.PlayerItem
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.databinding.ListItemPlayerBinding

class PlayersListAdapter(
    private val clickListener: (player: PlayerItem) -> Unit
): ListAdapter<PlayerItem, PlayersListAdapter.PlayerItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerItemViewHolder {
        return PlayerItemViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.list_item_player,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlayerItemViewHolder, position: Int) {
        val player = getItem(position)
        holder.itemView.setOnClickListener { clickListener(player) }
        holder.bind(player)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PlayerItem>() {
        override fun areItemsTheSame(oldItem: PlayerItem, newItem: PlayerItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PlayerItem, newItem: PlayerItem): Boolean {
            return oldItem == newItem
        }

    }

    class PlayerItemViewHolder(private val binding: ListItemPlayerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(player: PlayerItem) {
            binding.player = player
            binding.executePendingBindings()
        }
    }

}