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
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.database.Ascend
import com.yacgroup.yacguide.utils.AscendStyle

class AscentViewAdapter(
    context: Context,
    customSettings: SharedPreferences,
    private val _onClick: (Int) -> Unit)
    : ListAdapter<Ascend, RecyclerView.ViewHolder>(AscentDiffCallback) {

    private val _defaultBgColor = ContextCompat.getColor(context, R.color.colorSecondaryLight)
    private val _leadBgColor = customSettings.getInt(
        context.getString(R.string.lead),
        ContextCompat.getColor(context, R.color.color_lead))
    private val _followBgColor = customSettings.getInt(
        context.getString(R.string.follow),
        ContextCompat.getColor(context, R.color.color_follow))

    inner class AscentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _listItemLayout = view.findViewById<LinearLayout>(R.id.listItemLayout)
        private val _mainLeftTextView = view.findViewById<TextView>(R.id.mainLeftTextView)
        private val _subTextView = view.findViewById<TextView>(R.id.subTextView)

        fun bind(ascent: Ascend) {
            val bgColor =
                if (AscendStyle.isLead(AscendStyle.bitMask(ascent.styleId)))
                    _leadBgColor
                else if (AscendStyle.isFollow(AscendStyle.bitMask(ascent.styleId)))
                    _followBgColor
                else
                    _defaultBgColor
            _mainLeftTextView.text = "${ascent.day}.${ascent.month}.${ascent.year}"
            _subTextView.text = AscendStyle.fromId(ascent.styleId)?.styleName.orEmpty()
            _listItemLayout.apply {
                setBackgroundColor(bgColor)
                setOnClickListener {
                    _onClick(ascent.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_data_item, viewGroup, false)
        return AscentViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as AscentViewHolder).bind(getItem(position) as Ascend)
    }
}

object AscentDiffCallback : DiffUtil.ItemCallback<Ascend>() {
    override fun areItemsTheSame(oldAscent: Ascend, newAscent: Ascend): Boolean {
        return oldAscent.id == newAscent.id
    }

    override fun areContentsTheSame(oldAscent: Ascend, newAscent: Ascend): Boolean {
        return oldAscent == newAscent
    }
}