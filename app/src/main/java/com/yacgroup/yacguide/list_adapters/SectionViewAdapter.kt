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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.*
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.utils.VisualUtils

class SectionViewAdapter<T: Any>(
    private val _context: Context,
    private val _createInnerAdapter: () -> ListViewAdapter<T>
) : ListAdapter<SectionViewItem<T>, RecyclerView.ViewHolder>(SectionDiffCallback()) {

    inner class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _sectionHeaderLayout = view.findViewById<ConstraintLayout>(R.id.sectionHeaderLayout)
        private val _headerLeftTextView = view.findViewById<TextView>(R.id.sectionHeaderLeftTextView)
        private val _headerRightTextView = view.findViewById<TextView>(R.id.sectionHeaderRightTextView)
        private val _listView = view.findViewById<RecyclerView>(R.id.sectionRecyclerView)
        private val _listAdapter = _createInnerAdapter() // the inner adapter must be created here
                                                         // and not passed as parameter
        private val _viewPool = RecyclerView.RecycledViewPool() // for optimization
        private val _visualUtils: VisualUtils

        init {
            _listView.adapter = _listAdapter
            _visualUtils = VisualUtils(_context)
        }

        fun bind(sectionViewItem: SectionViewItem<T>) {
            _headerLeftTextView.text = sectionViewItem.title.first
            _headerRightTextView.text = sectionViewItem.title.second
            _sectionHeaderLayout.setBackgroundColor(_visualUtils.headerBgColor)

            val sectionLayoutManager = LinearLayoutManager(_listView.context, RecyclerView.VERTICAL, false)
            sectionLayoutManager.initialPrefetchItemCount = sectionViewItem.elements.size
            _listView.apply {
                layoutManager = sectionLayoutManager
                setRecycledViewPool(_viewPool)
            }
            _listAdapter.submitList(sectionViewItem.elements)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_view_section_content, viewGroup, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as SectionViewAdapter<T>.SectionViewHolder).bind(getItem(position) as SectionViewItem<T>)
    }
}

class SectionDiffCallback<T> : DiffUtil.ItemCallback<SectionViewItem<T>>() {
    override fun areItemsTheSame(oldViewItem: SectionViewItem<T>, newViewItem: SectionViewItem<T>): Boolean {
        return oldViewItem.title == newViewItem.title
    }

    override fun areContentsTheSame(oldViewItem: SectionViewItem<T>, newViewItem: SectionViewItem<T>): Boolean {
        return oldViewItem == newViewItem
    }
}