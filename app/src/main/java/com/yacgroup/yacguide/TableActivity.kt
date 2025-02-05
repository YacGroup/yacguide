/*
 * Copyright (C) 2019, 2022, 2023 Axel Paetzold
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

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewbinding.ViewBinding
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.comment.*
import com.yacgroup.yacguide.databinding.CommentBinding
import com.yacgroup.yacguide.databinding.CommentDialogBinding
import com.yacgroup.yacguide.databinding.CommentPropertyBinding
import com.yacgroup.yacguide.utils.*

abstract class TableActivity<ViewBindingType: ViewBinding> : BaseNavigationActivity<ViewBindingType>() {

    lateinit var activityLevel: ClimbingObject
    protected lateinit var db: DatabaseWrapper
    protected lateinit var customSettings: SharedPreferences
    protected lateinit var visualUtils: VisualUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityLevel = ClimbingObject(
            level = ClimbingObjectLevel.fromInt(intent.getIntExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eCountry.value)),
            parentUId = ClimbingObjectUId(
                id = intent.getIntExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, DatabaseWrapper.INVALID_ID),
                name = intent.getStringExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME).orEmpty()
            )
        )
        db = DatabaseWrapper(this)
        customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)
        visualUtils = VisualUtils(this, customSettings)
    }

    open fun showComments(v: View) {}

    override fun onResume() {
        super.onResume()

        displayContent()
    }

    protected fun showRegionComments(regionId: Int) {
        _showComments(db.getRegionComments(regionId).map {
            Comment(
                text = it.text.orEmpty(),
                properties = listOf(
                    CommentProperty(R.string.relevance, RegionComment.QUALITY_MAP, it.qualityId)
                )
            )
        })
    }

    protected fun showSectorComments(sectorId: Int) {
        _showComments(db.getSectorComments(sectorId).map {
            Comment(
                text = it.text.orEmpty(),
                properties = listOf(
                    CommentProperty(R.string.relevance, SectorComment.QUALITY_MAP, it.qualityId)
                )
            )
        })
    }

    protected fun showRockComments(rockId: Int) {
        _showComments(db.getRockComments(rockId).map {
            Comment(
                text = it.text.orEmpty(),
                properties = listOf(
                    CommentProperty(R.string.nature, RockComment.RELEVANCE_MAP, it.qualityId)
                )
            )
        })
    }

    protected fun showRouteComments(routeId: Int) {
        _showComments(db.getRouteComments(routeId).map {
            Comment(
                text = it.text.orEmpty(),
                properties = listOf(
                    CommentProperty(R.string.route_quality, RouteComment.QUALITY_MAP, it.qualityId),
                    CommentProperty(R.string.grade, RouteComment.GRADE_MAP, it.gradeId),
                    CommentProperty(R.string.protection, RouteComment.PROTECTION_MAP, it.securityId),
                    CommentProperty(R.string.drying, RouteComment.DRYING_MAP, it.wetnessId)
                )
            )
        })
    }

    protected fun showNoCommentToast() {
        Toast.makeText(this, R.string.no_comment_available, Toast.LENGTH_SHORT).show()
    }

    private fun _showComments(comments: List<Comment>) {
        if (comments.isNotEmpty()) {
            val dialog = DialogWidgetBuilder(this, R.string.comments).apply {
                setPositiveButton { dialog, _ -> dialog.dismiss() }
            }.create()
            val commentDialogBinding = CommentDialogBinding.inflate(dialog.layoutInflater)
            dialog.setView(commentDialogBinding.root)

            // FIXME: Why is this divider code necessary?
            val dividerResource = TypedValue()
            theme.resolveAttribute(android.R.attr.listDivider, dividerResource, true)
            comments.forEach { comment ->
                CommentBinding.inflate(dialog.layoutInflater).let { view ->
                    view.commentDivider.setBackgroundResource(dividerResource.resourceId)
                    comment.properties.forEach { prop ->
                        _property2String(prop.qualityMap, prop.qualityId)?.let { propValue ->
                            val propBinding = CommentPropertyBinding.inflate(dialog.layoutInflater).apply {
                                propertyNameTextView.setText(prop.nameResource)
                                propertyValueTextView.text = propValue
                            }
                            // The layout margins are not considered, if the view is add programmatically.
                            propBinding.root.layoutParams = ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.MATCH_PARENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                bottomMargin = 10
                            }
                            view.root.addView(
                                propBinding.root,
                                view.commentLayout.indexOfChild(view.commentTextView)
                            )
                        }
                    }
                    view.commentTextView.text = comment.text
                    commentDialogBinding.commentLayout.addView(view.root)
                }
            }
            dialog.show()
        } else {
            showNoCommentToast()
        }
    }

    private fun _property2String(propertyMap: Map<Int, String>, propertyKey: Int): String? {
        return if (propertyMap.containsKey(propertyKey) && propertyKey > RouteComment.NO_INFO_ID)
            propertyMap[propertyKey].orEmpty()
        else
            null
    }

    abstract fun displayContent()
}
