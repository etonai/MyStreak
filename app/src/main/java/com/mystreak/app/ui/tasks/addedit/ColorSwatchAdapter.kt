package com.mystreak.app.ui.tasks.addedit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mystreak.app.databinding.ItemColorSwatchBinding
import com.mystreak.app.util.ColorUtils

class ColorSwatchAdapter(
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<ColorSwatchAdapter.VH>() {

    private val colors = ColorUtils.palette
    private var selectedKey = ColorUtils.defaultKey()

    inner class VH(val binding: ItemColorSwatchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount() = colors.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemColorSwatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tc = colors[position]
        with(holder.binding) {
            val color = ContextCompat.getColor(root.context, tc.colorRes)
            viewSwatch.background.setTint(color)
            viewSelectedRing.visibility =
                if (tc.key == selectedKey) android.view.View.VISIBLE else android.view.View.GONE
            root.setOnClickListener {
                selectedKey = tc.key
                notifyDataSetChanged()
                onSelect(tc.key)
            }
        }
    }

    fun setSelected(key: String) {
        selectedKey = key
        notifyDataSetChanged()
    }

    fun getSelectedKey(): String = selectedKey
}
