/*
 * Copyright (C) 2019 Axel Paetzold
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

package com.yacgroup.yacguide

import android.annotation.SuppressLint
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuCompat
import androidx.viewbinding.ViewBinding
import com.yacgroup.yacguide.activity_properties.ActivityProperty

abstract class TableActivityWithOptionsMenu<ViewBindingType: ViewBinding> : TableActivity<ViewBindingType>() {

    protected lateinit var properties: ArrayList<ActivityProperty>

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        properties.map { menu.setGroupVisible(it.getMenuGroupId(), true) }
        MenuCompat.setGroupDividerEnabled(menu, true)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        for (prop in properties) {
            if (item.groupId == prop.getMenuGroupId()) {
                prop.onMenuAction(item.itemId)
                break
            }
        }
        return true
    }
}
