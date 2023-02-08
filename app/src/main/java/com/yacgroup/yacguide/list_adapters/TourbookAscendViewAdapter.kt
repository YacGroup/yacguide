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
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.database.*
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.ParserUtils

class TourbookAscendViewAdapter(
    context: Context,
    customSettings: SharedPreferences,
    private val _db: DatabaseWrapper,
    private val _onClick: (Int) -> Unit)
    : ListAdapter<Ascend, RecyclerView.ViewHolder>(AscendDiffCallback) {

    private val _defaultBgColor = ContextCompat.getColor(context, R.color.colorOnPrimary)
    private val _leadBgColor: Int
    private val _followBgColor: Int

    init {
        if (customSettings.getBoolean(context.getString(R.string.colorize_tourbook_entries),
                                      context.resources.getBoolean(R.bool.colorize_tourbook_entries))) {
            _leadBgColor = customSettings.getInt(context.getString(R.string.lead), _defaultBgColor)
            _followBgColor = customSettings.getInt(context.getString(R.string.follow), _defaultBgColor)
        } else {
            _leadBgColor = _defaultBgColor
            _followBgColor = _defaultBgColor
        }
    }

    inner class TourbookAscendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _listItemLayout = view.findViewById<LinearLayout>(R.id.listItemLayout)
        private val _mainLeftTextView = view.findViewById<TextView>(R.id.mainLeftTextView)
        private val _mainRightTextView = view.findViewById<TextView>(R.id.mainRightTextView)
        private val _subTextView = view.findViewById<TextView>(R.id.subTextView)

        fun bind(ascend: Ascend) {
            val route: Route = _db.getRoute(ascend.routeId) ?: _db.createUnknownRoute()
            val rock: Rock = _db.getRock(route.parentId) ?: _db.createUnknownRock()
            val routeName = ParserUtils.decodeObjectNames(route.name)
            val rockName = ParserUtils.decodeObjectNames(rock.name)
            val bgColor = AscendStyle.deriveAscentColor(
                ascentBitMask = route.ascendsBitMask,
                leadColor = _leadBgColor,
                followColor = _followBgColor,
                defaultColor = _defaultBgColor)

            _mainLeftTextView.text = "${rockName.first} - ${routeName.first}"
            _mainRightTextView.text = route.grade.orEmpty()
            _subTextView.text = "${rockName.second} - ${routeName.second}"
            _listItemLayout.apply {
                setBackgroundColor(bgColor)
                setOnClickListener {
                    _onClick(ascend.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_data_item, viewGroup, false)
        return TourbookAscendViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as TourbookAscendViewHolder).bind(getItem(position) as Ascend)
    }
}