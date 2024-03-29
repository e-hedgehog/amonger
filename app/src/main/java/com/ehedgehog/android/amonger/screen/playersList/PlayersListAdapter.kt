package com.ehedgehog.android.amonger.screen.playersList

import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ehedgehog.android.amonger.BuildConfig
import com.ehedgehog.android.amonger.R
import com.ehedgehog.android.amonger.databinding.ListItemPlayerBinding
import com.ehedgehog.android.amonger.screen.PlayerItem

class PlayersListAdapter(
    private val clickListener: (player: PlayerItem) -> Unit,
    private val contextMenuItemListener: (menuItem: MenuItem, player: PlayerItem) -> Boolean
): ListAdapter<PlayerItem, PlayersListAdapter.PlayerItemViewHolder>(DiffCallback) {

    var recyclerViewState: Parcelable? = null
        private set

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

    @Suppress("KotlinConstantConditions")
    override fun onBindViewHolder(holder: PlayerItemViewHolder, position: Int) {
        val player = getItem(position)
        holder.itemView.setOnClickListener { clickListener(player) }

        if (BuildConfig.FLAVOR != "user") {
            val popupMenu = PopupMenu(holder.itemView.context, holder.itemView, Gravity.END)
            popupMenu.inflate(R.menu.list_item_player_context)
            popupMenu.setOnMenuItemClickListener { contextMenuItemListener(it, player) }
            holder.itemView.setOnLongClickListener {
                popupMenu.show()
                true
            }
        }

        holder.bind(player)
    }

    fun saveRecyclerViewState(state: Parcelable?) {
        recyclerViewState = state
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
            binding.playerImage.clipToOutline = true
            binding.player = player
            if (player.imageUrl.isNullOrEmpty())
                binding.playerImage.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.placeholder))
            binding.executePendingBindings()
        }
    }

}