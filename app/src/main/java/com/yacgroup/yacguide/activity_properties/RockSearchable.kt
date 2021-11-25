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
import com.yacgroup.yacguide.SelectedRockActivity
import com.yacgroup.yacguide.TableActivity
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.utils.IntentConstants

class RockSearchable(private val _activity: TableActivity,
                     private val _searchRocks: () -> List<Rock>) : ActivityProperty {

    override fun getMenuGroupId() = R.id.group_rock_search

    override fun onMenuAction(menuItemId: Int) {
        val searchDialog = AppCompatDialog(_activity)
        searchDialog.setContentView(R.layout.search_dialog)
        searchDialog.findViewById<Button>(R.id.searchButton)?.setOnClickListener {
            val rockName = searchDialog.findViewById<EditText>(R.id.dialogEditText)?.text.toString().trim { it <= ' ' }
            if (rockName.isEmpty()) {
                Toast.makeText(searchDialog.context, R.string.hint_no_name, Toast.LENGTH_SHORT).show()
            } else {
                val rocks = _searchRocks().filter { rock -> rock.name!!.toLowerCase().contains(rockName.toLowerCase()) }
                if (rocks.isNotEmpty()) {
                    val rockIds = ArrayList(rocks.map { it.id })
                    val intent = Intent(_activity, SelectedRockActivity::class.java)
                    intent.putIntegerArrayListExtra(IntentConstants.SELECTED_ROCK_IDS, rockIds)
                    _activity.startActivity(intent)
                    searchDialog.dismiss()
                } else {
                    Toast.makeText(_activity, R.string.hint_rock_not_found, Toast.LENGTH_SHORT).show()
                }
            }
        }
        searchDialog.findViewById<Button>(R.id.cancelButton)?.setOnClickListener { searchDialog.dismiss() }
        searchDialog.setCancelable(false)
        searchDialog.setCanceledOnTouchOutside(false)
        searchDialog.show()
    }
}