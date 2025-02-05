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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.databinding.ListViewSectionContentBinding
import com.yacgroup.yacguide.utils.VisualUtils

data class SectionViewItem<T>(
    val title: Pair<String, String>,
    val elements: List<T>
)

class SectionViewAdapter<T: Any>(
    private val _visualUtils: VisualUtils,
    private val _swipeController: ItemTouchHelper.Callback? = null,
    private val _createInnerAdapter: () -> ListViewAdapter<T>
) : ListAdapter<SectionViewItem<T>, RecyclerView.ViewHolder>(SectionDiffCallback()) {

    inner class SectionViewHolder(binding: ListViewSectionContentBinding) : RecyclerView.ViewHolder(binding.root) {
        private val _sectionHeaderLayout = binding.sectionHeaderLayout
        private val _headerLeftTextView = binding.sectionHeaderLeftTextView
        private val _headerRightTextView = binding.sectionHeaderRightTextView
        private val _listView = binding.sectionRecyclerView
        private val _listAdapter = _createInnerAdapter() // the inner adapter must be created here
                                                         // and not passed as parameter
        private val _viewPool = RecyclerView.RecycledViewPool() // for optimization

        init {
            _swipeController?.let {
                ItemTouchHelper(it).attachToRecyclerView(_listView)
            }
            _listView.adapter = _listAdapter
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
        val binding = ListViewSectionContentBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return SectionViewHolder(binding)
    }

    @Suppress("UNCHECKED_CAST")
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
