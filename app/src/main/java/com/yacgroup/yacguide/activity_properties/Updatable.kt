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

import android.widget.ImageButton
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.TableActivity
import com.yacgroup.yacguide.UpdateHandler
import com.yacgroup.yacguide.network.JSONWebParser

class Updatable(private val _activity: TableActivity,
                jsonParser: JSONWebParser,
                deleteContent: () -> Unit) : ActivityProperty {

    private val _updateHandler = UpdateHandler(_activity, jsonParser, deleteContent, { _activity.displayContent() })

    override fun getMenuGroupId() = R.id.group_update

    override fun onMenuAction(menuItemId: Int) {
        when (menuItemId) {
            R.id.action_download -> _updateHandler.update()
            R.id.action_delete -> _updateHandler.delete()
        }
    }

    fun getDownloadButton(): ImageButton {
        val button = _activity.layoutInflater.inflate(R.layout.button_download, null) as ImageButton
        button.setOnClickListener{ _updateHandler.update() }
        return button
    }
}