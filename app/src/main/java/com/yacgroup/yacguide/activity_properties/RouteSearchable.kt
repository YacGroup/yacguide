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
import android.widget.*
import androidx.viewbinding.ViewBinding
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.RouteActivity
import com.yacgroup.yacguide.TableActivityWithOptionsMenu
import com.yacgroup.yacguide.database.comment.RouteComment
import com.yacgroup.yacguide.databinding.RouteSearchDialogBinding
import com.yacgroup.yacguide.utils.DialogWidgetBuilder
import com.yacgroup.yacguide.utils.FilterSpinner
import com.yacgroup.yacguide.utils.FilterSpinnerListener
import com.yacgroup.yacguide.utils.IntentConstants

class RouteSearchable<ViewBindingType: ViewBinding>(private val _activity: TableActivityWithOptionsMenu<ViewBindingType>) : ActivityProperty, FilterSpinnerListener {

    private var _minGradeId: Int = RouteComment.NO_INFO_ID
    private var _maxGradeId: Int = RouteComment.NO_INFO_ID
    private var _maxQualityId: Int = RouteComment.NO_INFO_ID
    private var _maxProtectionId: Int = RouteComment.NO_INFO_ID
    private var _maxDryingId: Int = RouteComment.NO_INFO_ID

    override fun getMenuGroupId() = R.id.group_route_search

    override fun onMenuAction(menuItemId: Int) {
        val searchDialog = DialogWidgetBuilder(_activity).create()
        val searchDialogBinding = RouteSearchDialogBinding.inflate(searchDialog.layoutInflater).also {
            searchDialog.setView(it.root)
        }

        FilterSpinner(searchDialog, searchDialogBinding.routeGradeFromSpinner, RouteComment.GRADE_MAP, this).create()
        FilterSpinner(searchDialog, searchDialogBinding.routeGradeToSpinner, RouteComment.GRADE_MAP, this).create()
        FilterSpinner(searchDialog, searchDialogBinding.routeQualitySpinner, RouteComment.QUALITY_MAP, this).create()
        FilterSpinner(searchDialog, searchDialogBinding.routeProtectionSpinner, RouteComment.PROTECTION_MAP, this).create()
        FilterSpinner(searchDialog, searchDialogBinding.routeDryingSpinner, RouteComment.DRYING_MAP, this).create()

        searchDialogBinding.searchButton.setOnClickListener {
            val routeName = searchDialogBinding.dialogEditText.text.toString().trim { it <= ' ' }
            if (routeName.isEmpty()
                && _minGradeId == RouteComment.NO_INFO_ID
                && _maxGradeId == RouteComment.NO_INFO_ID
                && _maxQualityId == RouteComment.NO_INFO_ID
                && _maxProtectionId == RouteComment.NO_INFO_ID
                && _maxDryingId == RouteComment.NO_INFO_ID
            ) {
                Toast.makeText(
                    searchDialog.context,
                    R.string.no_filter_selected,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                _activity.startActivity(Intent(_activity, RouteActivity::class.java).apply {
                    putExtra(
                        IntentConstants.CLIMBING_OBJECT_LEVEL,
                        _activity.activityLevel.level.value
                    )
                    putExtra(
                        IntentConstants.CLIMBING_OBJECT_PARENT_ID,
                        _activity.activityLevel.parentUId.id
                    )
                    putExtra(
                        IntentConstants.CLIMBING_OBJECT_PARENT_NAME,
                        _activity.activityLevel.parentUId.name
                    )
                    putExtra(IntentConstants.FILTER_NAME, routeName)
                    putExtra(IntentConstants.FILTER_GRADE_FROM, _minGradeId)
                    putExtra(IntentConstants.FILTER_GRADE_TO, _maxGradeId)
                    putExtra(IntentConstants.FILTER_RELEVANCE, _maxQualityId)
                    putExtra(IntentConstants.FILTER_PROTECTION, _maxProtectionId)
                    putExtra(IntentConstants.FILTER_DRYING, _maxDryingId)
                })
                searchDialog.dismiss()
            }
        }
        searchDialogBinding.cancelButton.setOnClickListener { searchDialog.dismiss() }

        searchDialog.show()
    }

    override fun onFilterSelected(resourceId: Int, selectionKey: Int) {
        when (resourceId) {
            R.id.routeGradeFromSpinner -> _minGradeId = selectionKey
            R.id.routeGradeToSpinner -> _maxGradeId = selectionKey
            R.id.routeQualitySpinner -> _maxQualityId = selectionKey
            R.id.routeProtectionSpinner -> _maxProtectionId = selectionKey
            R.id.routeDryingSpinner -> _maxDryingId = selectionKey
        }
    }
}
