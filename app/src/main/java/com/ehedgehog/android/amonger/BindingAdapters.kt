package com.ehedgehog.android.amonger

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.PopupMenu
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

interface OnItemClick {
    fun execute(item: CharSequence)
}

@BindingAdapter("addMenuItems", "defaultText", "onMenuItemClick", requireAll = false)
fun bindMenuChip(chip: Chip, menuItems: Array<String>, defaultText: String, onMenuItemClick: OnItemClick) {
    val menu = PopupMenu(chip.context, chip)
    menuItems.forEach { menu.menu.add(it) }
    chip.setOnClickListener { menu.show() }

    menu.setOnMenuItemClickListener {
        onMenuItemClick.execute(it.toString())
        val isSelected = it.toString() != menuItems[0]
        val isRepeatedClick = it.toString() == chip.text
        chip.text = if (isRepeatedClick || !isSelected) defaultText else it.toString()
        chip.isCheckable = isSelected && !isRepeatedClick
        chip.isChecked = isSelected && !isRepeatedClick
        true
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

@BindingAdapter("titlesList", "isSmallSize", requireAll = false)
fun createChipGroup(chipGroup: ChipGroup, chipTitles: List<String>?, smallSize: Boolean) {
    chipGroup.removeAllViews()
    chipTitles?.forEach {
        val context = chipGroup.context
        val textView = TextView(context)
        val size: Float
        val textSize: Float

        if (smallSize) {
            size = 24F
            textSize = 12F
        } else {
            size = 32F
            textSize = 14F
            textView.gravity = Gravity.CENTER
        }

        textView.setBackgroundResource(R.drawable.tag_shape)
        textView.minHeight = dpToPx(context, 10F).toInt()
        textView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, dpToPx(context, size).toInt())
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.text = it
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
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