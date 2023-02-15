/*
 * Copyright (C) 2022, 2023 Axel Paetzold
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
    val action: (viewHolder: RecyclerView.ViewHolder) -> Unit
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
        if (direction == ItemTouchHelper.RIGHT) {
            _onSwipeRightConfig.action(viewHolder)
        } else {
            _onSwipeLeftConfig.action(viewHolder)
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
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX > 0) {
                _onSwipeRightConfig.let {
                    val backgroundIconTop =
                        viewHolder.itemView.top + (viewHolder.itemView.height - it.background.intrinsicHeight) / 2
                    c.clipRect(0f, viewHolder.itemView.top.toFloat(), dX, viewHolder.itemView.bottom.toFloat())
                    c.drawColor(it.color)
                    it.background.bounds = Rect(
                        it.background.intrinsicWidth / 2,
                        backgroundIconTop,
                        3 * it.background.intrinsicWidth / 2,
                        backgroundIconTop + it.background.intrinsicHeight
                    )
                    it.background.draw(c)
                }
            } else {
                _onSwipeLeftConfig.let {
                    val backgroundIconTop =
                        viewHolder.itemView.top + (viewHolder.itemView.height - it.background.intrinsicHeight) / 2
                    c.clipRect(dX + viewHolder.itemView.width, viewHolder.itemView.top.toFloat(), viewHolder.itemView.width.toFloat(), viewHolder.itemView.bottom.toFloat())
                    c.drawColor(it.color)
                    it.background.bounds = Rect(
                        viewHolder.itemView.width - 3 * it.background.intrinsicWidth / 2,
                        backgroundIconTop,
                        viewHolder.itemView.width - it.background.intrinsicWidth / 2,
                        backgroundIconTop + it.background.intrinsicHeight
                    )
                    it.background.draw(c)
                }
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}