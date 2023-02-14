package com.ehedgehog.android.amonger

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ehedgehog.android.amonger.screen.PlayerItem
import com.ehedgehog.android.amonger.screen.playersList.PlayersListAdapter

@BindingAdapter("dataList")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<PlayerItem>?) {
    val adapter = recyclerView.adapter as PlayersListAdapter
    adapter.submitList(data)
}

@BindingAdapter("imageUrl", "cacheOnlyWeb", requireAll = false)
fun bindImageView(imageView: ImageView, url: String?, cacheOnlyWeb: Boolean?) {
    if (!url.isNullOrEmpty()) {
        Log.i("ImageTest", "url: $url")
        var builder = Glide.with(imageView.context)
            .load(url)

        if (cacheOnlyWeb == true && !url.startsWith("https"))
            builder = builder.skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)

        builder.placeholder(R.drawable.placeholder)
            .centerCrop()
            .fitCenter()
            .into(imageView)
    }
}