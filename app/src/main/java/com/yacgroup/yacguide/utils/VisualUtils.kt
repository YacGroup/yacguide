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

package com.yacgroup.yacguide.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import com.yacgroup.yacguide.R

class VisualUtils(
    context: Context,
    customSettings: SharedPreferences? = null,
    colorizeLeadsAndFollows: Boolean = true) {

    val defaultBgColor = ContextCompat.getColor(context, R.color.colorSecondaryLight)
    val headerBgColor = ContextCompat.getColor(context, R.color.colorSecondary)
    val accentBgColor = ContextCompat.getColor(context, R.color.colorAccentLight)
    val prohibitedBgColor = ContextCompat.getColor(context, R.color.colorSecondary)
    val leadBgColor = if (colorizeLeadsAndFollows)
            customSettings?.getInt(
                context.getString(R.string.lead),
                ContextCompat.getColor(context, R.color.color_lead)) ?: defaultBgColor
        else
            defaultBgColor
    val followBgColor = if (colorizeLeadsAndFollows)
            customSettings?.getInt(
                context.getString(R.string.follow),
                ContextCompat.getColor(context, R.color.color_follow)) ?: defaultBgColor
        else
            defaultBgColor
    val emptyBoxIcon = context.getString(R.string.empty_box)
    val tickedBoxIcon = context.getString(R.string.tick)
    val botchIcon = context.getString(R.string.botch)
    val projectIcon = context.getString(R.string.project)
    val watchingIcon = context.getString(R.string.watching)
    val arrow = context.getString(R.string.right_arrow)
    val startupArrow = context.getString(R.string.startup_arrow)
}