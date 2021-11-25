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
import android.widget.Toast
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.SelectedRockActivity
import com.yacgroup.yacguide.TableActivity
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.utils.IntentConstants

class AscentFilterable(private val _activity: TableActivity,
                       private val _searchProjects: () -> List<Rock>,
                       private val _searchBotches: () -> List<Rock>) : ActivityProperty {

    override fun getMenuGroupId() = R.id.group_filter

    override fun onMenuAction(menuItemId: Int) {
        when (menuItemId) {
            R.id.action_filter_projects -> _goToFilteredRocksView(_searchProjects())
            R.id.action_filter_botches -> _goToFilteredRocksView(_searchBotches())
        }
    }

    private fun _goToFilteredRocksView(rocks: List<Rock>): Boolean {
        if (rocks.isEmpty()) {
            Toast.makeText(_activity, R.string.hint_rock_not_found, Toast.LENGTH_SHORT).show()
            return false
        }
        val rockIds = ArrayList(rocks.map { it.id })
        val intent = Intent(_activity, SelectedRockActivity::class.java)
        intent.putIntegerArrayListExtra(IntentConstants.SELECTED_ROCK_IDS, rockIds)
        _activity.startActivity(intent)

        return true
    }
}