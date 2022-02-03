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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Region

class RegionViewAdapter(
    context: Context,
    private val _db: DatabaseWrapper,
    private val _onClick: (Int, String) -> Unit)
    : ListAdapter<Region, RecyclerView.ViewHolder>(RegionDiffCallback) {

    private val _emptyIcon = context.getString(R.string.empty_box)
    private val _tickedIcon = context.getString(R.string.tick)

    inner class RegionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _listItemLayout = view.findViewById<LinearLayout>(R.id.listItemLayout)
        private val _mainLeftTextView = view.findViewById<TextView>(R.id.mainLeftTextView)
        private val _mainRightTextView = view.findViewById<TextView>(R.id.mainRightTextView)

        fun bind(region: Region) {
            var icon = _emptyIcon
            var background = R.color.colorSecondaryLight
            if (_db.getSectors(region.id).isNotEmpty()) {
                icon = _tickedIcon
                background = R.color.colorAccentLight
            }
            _mainLeftTextView.text = region.name
            _mainRightTextView.text = icon
            _listItemLayout.apply {
                setBackgroundResource(background)
                setOnClickListener {
                    _onClick(region.id, region.name.orEmpty())
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_data_item, viewGroup, false)
        return RegionViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as RegionViewHolder).bind(getItem(position) as Region)
    }
}

object RegionDiffCallback : DiffUtil.ItemCallback<Region>() {
    override fun areItemsTheSame(oldRegion: Region, newRegion: Region): Boolean {
        return oldRegion.id == newRegion.id
    }

    override fun areContentsTheSame(oldRegion: Region, newRegion: Region): Boolean {
        // We need to return false here to always re-draw the list if returning from SectorActivity.
        // This is needed to enable highlighting of the according region if it has been downloaded
        // since the Region item itself will not have changed by that.
        return false
    }
}