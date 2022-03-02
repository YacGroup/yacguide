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

package com.yacgroup.yacguide.activity_properties

import android.content.Intent
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDialog
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.RouteActivity
import com.yacgroup.yacguide.TableActivityWithOptionsMenu
import com.yacgroup.yacguide.database.comment.RouteComment
import com.yacgroup.yacguide.utils.FilterSpinner
import com.yacgroup.yacguide.utils.FilterSpinnerListener
import com.yacgroup.yacguide.utils.IntentConstants

class RouteSearchable(private val _activity: TableActivityWithOptionsMenu) : ActivityProperty, FilterSpinnerListener {

    private var _maxQualityId: Int = RouteComment.QUALITY_NONE
    private var _maxProtectionId: Int = RouteComment.PROTECTION_NONE

    override fun getMenuGroupId() = R.id.group_route_search

    override fun onMenuAction(menuItemId: Int) {
        val searchDialog = AppCompatDialog(_activity, R.style.AppTheme_Dialog)
        searchDialog.setContentView(R.layout.route_search_dialog)
        searchDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        FilterSpinner(searchDialog, R.id.routeQualitySpinner, RouteComment.QUALITY_MAP, this).create()
        FilterSpinner(searchDialog, R.id.routeProtectionSpinner, RouteComment.PROTECTION_MAP, this).create()

        searchDialog.findViewById<Button>(R.id.searchButton)?.setOnClickListener {
            val routeName = searchDialog.findViewById<EditText>(R.id.dialogEditText)?.text.toString().trim { it <= ' ' }
            if (routeName.isEmpty()
                && _maxQualityId == RouteComment.QUALITY_NONE
                && _maxProtectionId == RouteComment.PROTECTION_NONE) {
                    Toast.makeText(searchDialog.context, R.string.no_filter_selected, Toast.LENGTH_SHORT).show()
            } else {
                _activity.startActivity(Intent(_activity, RouteActivity::class.java).apply {
                    putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, _activity.activityLevel.level.value)
                    putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, _activity.activityLevel.parentId)
                    putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, _activity.activityLevel.parentName)
                    putExtra(IntentConstants.FILTER_NAME, routeName)
                    putExtra(IntentConstants.FILTER_RELEVANCE, _maxQualityId)
                    putExtra(IntentConstants.FILTER_PROTECTION, _maxProtectionId)
                })
                searchDialog.dismiss()
            }
        }
        searchDialog.findViewById<Button>(R.id.cancelButton)?.setOnClickListener { searchDialog.dismiss() }
        searchDialog.setCancelable(false)
        searchDialog.setCanceledOnTouchOutside(false)
        searchDialog.show()
    }

    override fun onFilterSelected(resourceId: Int, selectionKey: Int) {
        when (resourceId) {
            R.id.routeQualitySpinner -> _maxQualityId = selectionKey
            R.id.routeProtectionSpinner -> _maxProtectionId = selectionKey
        }
    }
}