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
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.ClimbingObjectLevel
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.ParserUtils

class RockViewAdapter(
    context: Context,
    customSettings: SharedPreferences,
    private val _climbingObjectLevel: ClimbingObjectLevel,
    private val _db: DatabaseWrapper,
    private val _onClick: (Int, String) -> Unit)
    : ListAdapter<Rock, RecyclerView.ViewHolder>(RockDiffCallback) {

    private val _defaultBgColor = ContextCompat.getColor(context, R.color.colorSecondaryLight)
    private val _prohibitedBgColor = ContextCompat.getColor(context, R.color.colorSecondary)
    private val _leadBgColor = customSettings.getInt(
        context.getString(R.string.lead),
        ContextCompat.getColor(context, R.color.color_lead))
    private val _followBgColor = customSettings.getInt(
        context.getString(R.string.follow),
        ContextCompat.getColor(context, R.color.color_follow))
    private val _botchIcon = context.getString(R.string.botch)
    private val _projectIcon = context.getString(R.string.project)
    private val _watchingIcon = context.getString(R.string.watching)
    private val _arrow = context.getString(R.string.right_arrow)

    inner class RockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _listItemLayout = view.findViewById<LinearLayout>(R.id.listItemLayout)
        private val _infoTextLayout = view.findViewById<ConstraintLayout>(R.id.infoTextLayout)
        private val _infoLeftTextView = view.findViewById<TextView>(R.id.infoLeftTextView)
        private val _mainLeftTextView = view.findViewById<TextView>(R.id.mainLeftTextView)
        private val _mainRightTextView = view.findViewById<TextView>(R.id.mainRightTextView)
        private val _subTextView = view.findViewById<TextView>(R.id.subTextView)

        fun bind(rock: Rock) {
            val rockName = ParserUtils.decodeObjectNames(rock.name)
            var bgColor = _defaultBgColor
            var typeface = Typeface.BOLD
            var typeAdd = ""
            if (rock.type != Rock.typeSummit) {
                typeface = Typeface.NORMAL
                typeAdd = "  (${rock.type})"
            }
            if (rock.status == Rock.statusProhibited || rock.status == Rock.statusCollapsed) {
                typeface = Typeface.ITALIC
                bgColor = _prohibitedBgColor
            }
            bgColor = AscendStyle.deriveAscendColor(
                rock.ascendsBitMask,
                _leadBgColor,
                _followBgColor,
                defaultColor = bgColor)
            val decorationAdd = AscendStyle.deriveAscendDecoration(
                rock.ascendsBitMask,
                _botchIcon,
                _projectIcon,
                _watchingIcon
            )
            val sectorInfo = _getSectorInfo(rock)
            if (sectorInfo.isNotEmpty()) {
                _infoTextLayout.visibility = View.VISIBLE
                _infoLeftTextView.text = sectorInfo
            }
            _mainLeftTextView.setTypeface(null, typeface)
            _mainLeftTextView.text = "${rock.nr}  ${rockName.first}$typeAdd$decorationAdd"
            _mainRightTextView.setTypeface(null, typeface)
            _mainRightTextView.text = rock.status.toString()
            _subTextView.setTypeface(null, typeface)
            _subTextView.text = rockName.second
            _listItemLayout.apply {
                setBackgroundColor(bgColor)
                setOnClickListener {
                    _onClick(rock.id, rock.name.orEmpty())
                }
            }
        }

        private fun _getSectorInfo(rock: Rock): String {
            var sectorInfo = ""
            if (_climbingObjectLevel < ClimbingObjectLevel.eRock) {
                val sector = _db.getSector(rock.parentId)!!
                if (_climbingObjectLevel < ClimbingObjectLevel.eSector) {
                    val region = _db.getRegion(sector.parentId)!!
                    sectorInfo = "${region.name} $_arrow "
                }
                val sectorNames = ParserUtils.decodeObjectNames(sector.name)
                sectorInfo += sectorNames.first
                if (sectorNames.second.isNotEmpty()) {
                    sectorInfo += " / ${sectorNames.second}"
                }
            }
            return sectorInfo
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_data_item, viewGroup, false)
        return RockViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as RockViewHolder).bind(getItem(position) as Rock)
    }
}

object RockDiffCallback : DiffUtil.ItemCallback<Rock>() {
    override fun areItemsTheSame(oldRock: Rock, newRock: Rock): Boolean {
        return oldRock.id == newRock.id
    }

    override fun areContentsTheSame(oldRock: Rock, newRock: Rock): Boolean {
        return oldRock == newRock
    }
}