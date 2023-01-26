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

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.comment.*
import com.yacgroup.yacguide.utils.*

abstract class TableActivity : BaseNavigationActivity() {

    lateinit var activityLevel: ClimbingObject
    protected lateinit var db: DatabaseWrapper
    protected lateinit var customSettings: SharedPreferences

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
                    CommentProperty(R.id.qualityLayout, R.string.relevance, RegionComment.QUALITY_MAP, it.qualityId)
                )
            )
        })
    }

    protected fun showSectorComments(sectorId: Int) {
        _showComments(db.getSectorComments(sectorId).map {
            Comment(
                text = it.text.orEmpty(),
                properties = listOf(
                    CommentProperty(R.id.qualityLayout, R.string.relevance, SectorComment.QUALITY_MAP, it.qualityId)
                )
            )
        })
    }

    protected fun showRockComments(rockId: Int) {
        _showComments(db.getRockComments(rockId).map {
            Comment(
                text = it.text.orEmpty(),
                properties = listOf(
                    CommentProperty(R.id.qualityLayout, R.string.nature, RockComment.RELEVANCE_MAP, it.qualityId)
                )
            )
        })
    }

    protected fun showRouteComments(routeId: Int) {
        _showComments(db.getRouteComments(routeId).map {
            Comment(
                text = it.text.orEmpty(),
                properties = listOf(
                    CommentProperty(R.id.qualityLayout, R.string.route_quality, RouteComment.QUALITY_MAP, it.qualityId),
                    CommentProperty(R.id.gradeLayout, R.string.grade, RouteComment.GRADE_MAP, it.gradeId),
                    CommentProperty(R.id.protectionLayout, R.string.protection, RouteComment.PROTECTION_MAP, it.securityId),
                    CommentProperty(R.id.dryingLayout, R.string.drying, RouteComment.DRYING_MAP, it.wetnessId)
                )
            )
        })
    }

    protected fun showNoCommentToast() {
        Toast.makeText(this, R.string.no_comment_available, Toast.LENGTH_SHORT).show()
    }

    protected fun colorizeEntry(ascendsBitMask: Int, defaultColor: Int): Int {
        return when {
            AscendStyle.isLead(ascendsBitMask) -> customSettings.getInt(getString(R.string.lead), ContextCompat.getColor(this, R.color.color_lead))
            AscendStyle.isFollow(ascendsBitMask) -> customSettings.getInt(getString(R.string.follow), ContextCompat.getColor(this, R.color.color_follow))
            else -> defaultColor
        }
    }

    protected fun decorateEntry(ascendsBitMask: Int): String {
        val botchAdd = if (AscendStyle.isBotch(ascendsBitMask)) getString(R.string.botch) else ""
        val projectAdd = if (AscendStyle.isProject(ascendsBitMask)) getString(R.string.project) else ""
        val watchingAdd = if (AscendStyle.isWatching(ascendsBitMask)) getString(R.string.watching) else ""
        return "$botchAdd$projectAdd$watchingAdd"
    }

    private fun _showComments(comments: List<Comment>) {
        if (comments.isNotEmpty()) {
            val dialog = _prepareCommentDialog()
            val layout = dialog.findViewById<LinearLayout>(R.id.commentLayout)
            val dividerResource = TypedValue()
            theme.resolveAttribute(android.R.attr.listDivider, dividerResource, true)
            comments.forEach { comment ->
                dialog.layoutInflater.inflate(R.layout.comment, layout, false).let { view ->
                    val commentLayout = view.findViewById<LinearLayout>(R.id.singleCommentLayout)
                    commentLayout.findViewById<View>(R.id.commentDivider).setBackgroundResource(dividerResource.resourceId)
                    comment.properties.forEach { prop ->
                        _property2String(prop.qualityMap, prop.qualityId)?.let {
                            val propertyLayout = commentLayout.findViewById<ConstraintLayout>(prop.layoutResource)
                            propertyLayout.findViewById<TextView>(R.id.propertyNameTextView).setText(prop.nameResource)
                            propertyLayout.findViewById<TextView>(R.id.propertyValueTextView).text = it
                            propertyLayout.visibility = View.VISIBLE
                        }
                    }
                    commentLayout.findViewById<TextView>(R.id.commentTextView).text = comment.text
                    layout?.addView(commentLayout)
                }
            }
        } else {
            showNoCommentToast()
        }
    }

    private fun _prepareCommentDialog(): AlertDialog {
        val dialog = DialogWidgetBuilder(this, R.string.comments).apply {
            setPositiveButton { dialog, _ ->
                dialog.dismiss()
            }
            setView(R.layout.comment_dialog)
        }.create()
        // we need to call show() before findViewById can be used.
        dialog.show()
        return dialog
    }

    private fun _property2String(propertyMap: Map<Int, String>, propertyKey: Int): String? {
        return if (propertyMap.containsKey(propertyKey) && propertyKey > RouteComment.NO_INFO_ID)
            propertyMap[propertyKey].orEmpty()
        else
            null
    }

    abstract fun displayContent()
}
