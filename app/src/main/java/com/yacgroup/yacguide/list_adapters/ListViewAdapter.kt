/*
 * Copyright (C) 2023 Axel Paetzold
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

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.R

data class ListItem(
    val backgroundColor: Int = 0,
    val typeface: Int = Typeface.BOLD,
    val titleText: Pair<String, String>? = null,
    val mainText: Pair<String, String> = Pair("", ""),
    val subText: String? = "",
    val onClick: () -> Unit = {}
)

class ListViewAdapter<T: Any>(
    itemDiffCallback: ItemDiffCallback<T>,
    private val _generateListItem: (item: T) -> ListItem)
    : ListAdapter<T, RecyclerView.ViewHolder>(itemDiffCallback) {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _listItemLayout = view.findViewById<LinearLayout>(R.id.listItemLayout)
        private val _infoTextLayout = view.findViewById<ConstraintLayout>(R.id.infoTextLayout)
        private val _infoLeftTextView = view.findViewById<TextView>(R.id.infoLeftTextView)
        private val _infoRightTextView = view.findViewById<TextView>(R.id.infoRightTextView)
        private val _mainLeftTextView = view.findViewById<TextView>(R.id.mainLeftTextView)
        private val _mainRightTextView = view.findViewById<TextView>(R.id.mainRightTextView)
        private val _subTextLayout = view.findViewById<ConstraintLayout>(R.id.subTextLayout)
        private val _subTextView = view.findViewById<TextView>(R.id.subTextView)
        private var _item: T? = null

        fun bind(item: T) {
            _item = item
            val listItem = _generateListItem(item)
            _listItemLayout.apply {
                val bgGradient = background as GradientDrawable
                background = bgGradient.apply {
                    mutate()
                    colors = intArrayOf(listItem.backgroundColor, bgGradient.colors?.get(1)?:0)
                }
                setOnClickListener { listItem.onClick() }
            }
            listItem.titleText?.let {
                _infoTextLayout.visibility = View.VISIBLE
                _infoLeftTextView.text = listItem.titleText.first
                _infoRightTextView.text = listItem.titleText.second
            }
            listItem.typeface.let {
                _mainLeftTextView.setTypeface(null, it)
                _mainRightTextView.setTypeface(null, it)
                _subTextView.setTypeface(null, it)
            }
            _mainLeftTextView.text = listItem.mainText.first
            _mainRightTextView.text = listItem.mainText.second
            listItem.subText?.let {
                _subTextLayout.visibility = View.VISIBLE
                _subTextView.text = it
            }
        }

        fun getItem() = _item

        fun getParentAdapter() = this@ListViewAdapter
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_data_item, viewGroup, false)
        return ItemViewHolder(view)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as ListViewAdapter<T>.ItemViewHolder).bind(getItem(position) as T)
    }
}

class ItemDiffCallback<T: Any>(
    private val _areItemsTheSame: (item1: T, item2: T) -> Boolean = { _, _ -> false },
    private val _areContentsTheSame: (item1: T, item2: T) -> Boolean = { _, _ -> false}
) : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return _areItemsTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return _areContentsTheSame(oldItem, newItem)
    }
}
