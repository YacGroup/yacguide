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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.R

class BaseViewAdapter(private val _onClick: (Int) -> Unit)
    : ListAdapter<BaseViewItem, RecyclerView.ViewHolder>(BaseItemDiffCallback) {

    inner class BaseItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _listItemLayout = view.findViewById<LinearLayout>(R.id.listItemLayout)
        private val _mainLeftTextView = view.findViewById<TextView>(R.id.mainLeftTextView)
        private val _mainRightTextView = view.findViewById<TextView>(R.id.mainRightTextView)
        private val _subTextView = view.findViewById<TextView>(R.id.subTextView)
        private var _isHeader = false

        fun bind(viewItem: BaseViewItem) {
            _isHeader = viewItem.isHeader
            _listItemLayout.apply {
                setBackgroundResource(viewItem.backgroundResource)
            }
            if (_isHeader) {
                _mainRightTextView.text = viewItem.name
                _subTextView.visibility = View.GONE
            } else {
                _mainLeftTextView.text = viewItem.name
                _subTextView.text = viewItem.additionalInfo
                _listItemLayout.apply {
                    setOnClickListener {
                        _onClick(viewItem.id)
                    }
                }
            }
        }

        fun isHeader() = _isHeader
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_data_item, viewGroup, false)
        return BaseItemViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as BaseItemViewHolder).bind(getItem(position) as BaseViewItem)
    }

    fun getItemAt(position: Int) = getItem(position) as BaseViewItem
}

object BaseItemDiffCallback : DiffUtil.ItemCallback<BaseViewItem>() {
    override fun areItemsTheSame(oldViewItem: BaseViewItem, newViewItem: BaseViewItem): Boolean {
        return oldViewItem.id == newViewItem.id
    }

    override fun areContentsTheSame(oldViewItem: BaseViewItem, newViewItem: BaseViewItem): Boolean {
        return oldViewItem == newViewItem
    }
}