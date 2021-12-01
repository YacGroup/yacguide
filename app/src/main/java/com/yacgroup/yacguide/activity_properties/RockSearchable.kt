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
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.RockActivity
import com.yacgroup.yacguide.TableActivityWithOptionsMenu
import com.yacgroup.yacguide.utils.IntentConstants

class RockSearchable(private val _activity: TableActivityWithOptionsMenu) : ActivityProperty {

    override fun getMenuGroupId() = R.id.group_rock_search

    override fun onMenuAction(menuItemId: Int) {
        val searchDialog = AppCompatDialog(_activity)
        searchDialog.setContentView(R.layout.search_dialog)
        searchDialog.findViewById<Button>(R.id.searchButton)?.setOnClickListener {
            val rockName = searchDialog.findViewById<EditText>(R.id.dialogEditText)?.text.toString().trim { it <= ' ' }
            if (rockName.isEmpty()) {
                Toast.makeText(searchDialog.context, R.string.hint_no_name, Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(_activity, RockActivity::class.java)
                intent.putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, _activity.activityLevel.level.value)
                intent.putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, _activity.activityLevel.parentId)
                intent.putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, _activity.activityLevel.parentName)
                intent.putExtra(IntentConstants.FILTER_NAME, rockName)
                _activity.startActivity(intent)
                searchDialog.dismiss()
            }
        }
        searchDialog.findViewById<Button>(R.id.cancelButton)?.setOnClickListener { searchDialog.dismiss() }
        searchDialog.setCancelable(false)
        searchDialog.setCanceledOnTouchOutside(false)
        searchDialog.show()
    }
}