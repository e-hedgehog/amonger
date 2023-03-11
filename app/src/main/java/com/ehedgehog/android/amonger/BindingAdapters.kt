package com.ehedgehog.android.amonger

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.updateMarginsRelative
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ehedgehog.android.amonger.screen.PlayerItem
import com.ehedgehog.android.amonger.screen.playersList.PlayersListAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

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

        builder.centerCrop()
            .fitCenter()
            .into(imageView)
    }
}

@BindingAdapter("checkedList")
fun bindChipGroup(chipGroup: ChipGroup, checkedList: List<String>?) {
    chipGroup.forEach { view ->
        val chip: Chip = view as Chip
        if (checkedList?.contains(chip.text) == true) {
            chip.isChecked = true
        }
    }
}

@BindingAdapter("titlesList")
fun createChipGroup(chipGroup: ChipGroup, chipTitles: List<String>?) {
    chipTitles?.forEach {
        val context = chipGroup.context
        val textView = TextView(context)
        textView.setBackgroundResource(R.drawable.tag_shape)
        textView.minHeight = dpToPx(context, 10F).toInt()
        textView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, dpToPx(context, 24F).toInt())
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.text = it
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F)
        textView.setTextColor(ContextCompat.getColor(context, R.color.text_color))
        chipGroup.addView(textView)
    }
}

private fun dpToPx(context: Context, dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
}

@BindingAdapter("layoutMarginStart")
fun setLayoutMarginStart(view: View, dimen: Float) {
    val layoutParams = view.layoutParams as MarginLayoutParams
    layoutParams.updateMarginsRelative(dimen.toInt())
    view.layoutParams = layoutParams
}