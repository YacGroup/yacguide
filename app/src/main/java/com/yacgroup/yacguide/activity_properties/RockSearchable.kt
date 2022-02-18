/*
 * Copyright (C) 2021, 2022 Axel Paetzold
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
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDialog
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.RockActivity
import com.yacgroup.yacguide.TableActivityWithOptionsMenu
import com.yacgroup.yacguide.database.comment.RockComment
import com.yacgroup.yacguide.utils.IntentConstants

class RockSearchable(private val _activity: TableActivityWithOptionsMenu) : ActivityProperty {

    override fun getMenuGroupId() = R.id.group_rock_search

    override fun onMenuAction(menuItemId: Int) {
        val searchDialog = AppCompatDialog(_activity, R.style.AppTheme_Dialog)
        searchDialog.setContentView(R.layout.rock_search_dialog)
        searchDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        var maxRelevanceId = RockComment.RELEVANCE_NONE

        val spinner = searchDialog.findViewById<Spinner>(R.id.rockRelevanceSpinner)
        val adapter = ArrayAdapter<CharSequence>(searchDialog.context, R.layout.spinner_item, RockComment.RELEVANCE_MAP.values.toTypedArray())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val qualityName = parent.getItemAtPosition(position).toString()
                maxRelevanceId = RockComment.RELEVANCE_MAP.keys.first { RockComment.RELEVANCE_MAP[it] == qualityName }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        spinner?.setSelection(adapter.getPosition(RockComment.RELEVANCE_MAP[maxRelevanceId]))

        searchDialog.findViewById<Button>(R.id.searchButton)?.setOnClickListener {
            val rockName = searchDialog.findViewById<EditText>(R.id.dialogEditText)?.text.toString().trim { it <= ' ' }

            _activity.startActivity(Intent(_activity, RockActivity::class.java).apply {
                putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, _activity.activityLevel.level.value)
                putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, _activity.activityLevel.parentId)
                putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, _activity.activityLevel.parentName)
                putExtra(IntentConstants.FILTER_NAME, rockName)
                putExtra(IntentConstants.FILTER_RELEVANCE, maxRelevanceId)
            })

            searchDialog.dismiss()
        }

        searchDialog.findViewById<Button>(R.id.cancelButton)?.setOnClickListener { searchDialog.dismiss() }
        searchDialog.setCancelable(false)
        searchDialog.setCanceledOnTouchOutside(false)
        searchDialog.show()
    }
}