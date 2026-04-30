package com.mystreak.app.ui.calendar

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SquareCellDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        // Force each cell to be square by setting height = width via layout params
        val columnWidth = parent.width / 7
        val lp = view.layoutParams
        lp.height = columnWidth
        view.layoutParams = lp
    }
}
