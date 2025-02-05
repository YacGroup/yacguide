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
import android.widget.*
import androidx.viewbinding.ViewBinding
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.RockActivity
import com.yacgroup.yacguide.TableActivityWithOptionsMenu
import com.yacgroup.yacguide.database.comment.RockComment
import com.yacgroup.yacguide.databinding.RockSearchDialogBinding
import com.yacgroup.yacguide.utils.DialogWidgetBuilder
import com.yacgroup.yacguide.utils.FilterSpinner
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.FilterSpinnerListener

class RockSearchable<ViewBindingType: ViewBinding>(private val _activity: TableActivityWithOptionsMenu<ViewBindingType>) : ActivityProperty, FilterSpinnerListener {

    private var _maxRelevanceId: Int = RockComment.NO_INFO_ID

    override fun getMenuGroupId() = R.id.group_rock_search

    override fun onMenuAction(menuItemId: Int) {
        val searchDialog = DialogWidgetBuilder(_activity).create()
        RockSearchDialogBinding.inflate(searchDialog.layoutInflater).also {
            searchDialog.setView(it.root)

            FilterSpinner(
                searchDialog,
                it.rockRelevanceSpinner,
                RockComment.RELEVANCE_MAP,
                this
            ).create()

            it.searchButton.setOnClickListener { _ ->
                val rockName = it.dialogEditText.text.toString().trim { x -> x <= ' ' }
                if (rockName.isEmpty() && _maxRelevanceId == RockComment.NO_INFO_ID) {
                    Toast.makeText(
                        searchDialog.context,
                        R.string.no_filter_selected,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    _activity.startActivity(Intent(_activity, RockActivity::class.java).apply {
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
                        putExtra(IntentConstants.FILTER_NAME, rockName)
                        putExtra(IntentConstants.FILTER_RELEVANCE, _maxRelevanceId)
                    })
                    searchDialog.dismiss()
                }
            }
            it.cancelButton.setOnClickListener { searchDialog.dismiss() }
        }
        searchDialog.show()
    }

    override fun onFilterSelected(resourceId: Int, selectionKey: Int) {
        when (resourceId) {
            R.id.rockRelevanceSpinner -> _maxRelevanceId = selectionKey
        }
    }
}
