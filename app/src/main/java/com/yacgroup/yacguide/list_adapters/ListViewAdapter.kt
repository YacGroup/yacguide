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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.databinding.ListDataItemBinding

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

    inner class ItemViewHolder(binding: ListDataItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val _listItemLayout = binding.listItemLayout
        private val _infoTextLayout = binding.infoTextLayout
        private val _infoLeftTextView = binding.infoLeftTextView
        private val _infoRightTextView = binding.infoRightTextView
        private val _mainLeftTextView = binding.mainLeftTextView
        private val _mainRightTextView = binding.mainRightTextView
        private val _subTextLayout = binding.subTextLayout
        private val _subTextView = binding.subTextView
        private var _item: T? = null

        fun bind(item: T) {
            _item = item
            val listItem = _generateListItem(item)
            _listItemLayout.apply {
                setBackgroundColor(listItem.backgroundColor)
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
        val binding = ListDataItemBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ItemViewHolder(binding)
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
