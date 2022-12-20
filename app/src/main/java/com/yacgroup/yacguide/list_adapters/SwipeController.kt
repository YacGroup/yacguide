/*
 * Copyright (C) 2022 Axel Paetzold
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.yacgroup.yacguide.list_adapters

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

data class SwipeConfig(
    val color: Int,
    val background: Drawable,
    val action: (position: Int) -> Unit
)

class SwipeController(
    private val _onSwipeRightConfig: SwipeConfig,
    private val _onSwipeLeftConfig: SwipeConfig
    ) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (!(viewHolder as BaseViewAdapter.BaseItemViewHolder).isHeader()) {
            if (direction == ItemTouchHelper.RIGHT) {
                _onSwipeRightConfig.action(viewHolder.adapterPosition)
            } else {
                _onSwipeLeftConfig.action(viewHolder.adapterPosition)
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (!(viewHolder as BaseViewAdapter.BaseItemViewHolder).isHeader() && actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX > 0) {
                val backgroundIconTop =
                    viewHolder.itemView.top + (viewHolder.itemView.height - _onSwipeRightConfig.background.intrinsicHeight) / 2
                c.clipRect(0f, viewHolder.itemView.top.toFloat(), dX, viewHolder.itemView.bottom.toFloat())
                c.drawColor(_onSwipeRightConfig.color)
                _onSwipeRightConfig.background.bounds = Rect(
                    _onSwipeRightConfig.background.intrinsicWidth / 2,
                    backgroundIconTop,
                    3 * _onSwipeRightConfig.background.intrinsicWidth / 2,
                    backgroundIconTop + _onSwipeRightConfig.background.intrinsicHeight
                )
                _onSwipeRightConfig.background.draw(c)
            } else {
                val backgroundIconTop =
                    viewHolder.itemView.top + (viewHolder.itemView.height - _onSwipeLeftConfig.background.intrinsicHeight) / 2
                c.clipRect(dX + viewHolder.itemView.width, viewHolder.itemView.top.toFloat(), viewHolder.itemView.width.toFloat(), viewHolder.itemView.bottom.toFloat())
                c.drawColor(_onSwipeLeftConfig.color)
                _onSwipeLeftConfig.background.bounds = Rect(
                    viewHolder.itemView.width - 3 * _onSwipeLeftConfig.background.intrinsicWidth / 2,
                    backgroundIconTop,
                    viewHolder.itemView.width - _onSwipeLeftConfig.background.intrinsicWidth / 2,
                    backgroundIconTop + _onSwipeLeftConfig.background.intrinsicHeight
                )
                _onSwipeLeftConfig.background.draw(c)
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

    }

}