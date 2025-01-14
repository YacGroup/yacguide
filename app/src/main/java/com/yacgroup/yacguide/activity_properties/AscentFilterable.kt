/*
 * Copyright (C) 2021 Axel Paetzold
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

package com.yacgroup.yacguide.activity_properties

import android.content.Intent
import androidx.viewbinding.ViewBinding
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.RouteActivity
import com.yacgroup.yacguide.TableActivityWithOptionsMenu
import com.yacgroup.yacguide.utils.IntentConstants

class AscentFilterable<bindingType: ViewBinding>(private val _activity: TableActivityWithOptionsMenu<bindingType>) : ActivityProperty {

    override fun getMenuGroupId() = R.id.group_filter

    override fun onMenuAction(menuItemId: Int) {
        when (menuItemId) {
            R.id.action_filter_projects -> _goToFilteredRoutesView(IntentConstants.FILTER_PROJECTS)
            R.id.action_filter_botches -> _goToFilteredRoutesView(IntentConstants.FILTER_BOTCHES)
        }
    }

    private fun _goToFilteredRoutesView(filterIntent: String) {
        _activity.startActivity(
            Intent(_activity, RouteActivity::class.java).apply {
                putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, _activity.activityLevel.level.value)
                putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, _activity.activityLevel.parentUId.id)
                putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, _activity.activityLevel.parentUId.name)
                putExtra(filterIntent, true)
            }
        )
    }
}
