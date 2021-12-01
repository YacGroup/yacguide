/*
 * Copyright (C) 2019 Fabian Kantereit
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
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat

import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.comment.*
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.DialogWidgetBuilder
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

abstract class TableActivity : BaseNavigationActivity() {

    lateinit var activityLevel: ClimbingObject
    protected lateinit var db: DatabaseWrapper
    protected lateinit var customSettings: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityLevel = ClimbingObject(
            level = ClimbingObjectLevel.fromInt(intent.getIntExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eCountry.value)),
            parentId = intent.getIntExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, DatabaseWrapper.INVALID_ID),
            parentName = intent.getStringExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME).orEmpty()
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
        val comments = db.getRegionComments(regionId)
        if (comments.isNotEmpty()) {
            _prepareCommentDialog().findViewById<LinearLayout>(R.id.commentLayout)?.let {
                for (comment in comments) {
                    it.addView(WidgetUtils.createHorizontalLine(this, 1))
                    _addCommentPropertyView(it, R.string.relevance, RegionComment.QUALITY_MAP, comment.qualityId)
                    _addCommentTextView(it, comment.text.orEmpty())
                }
                it.addView(WidgetUtils.createHorizontalLine(this, 1))
            }
        } else {
            showNoCommentToast()
        }
    }

    protected fun showSectorComments(sectorId: Int) {
        val comments = db.getSectorComments(sectorId)
        if (comments.isNotEmpty()) {
            _prepareCommentDialog().findViewById<LinearLayout>(R.id.commentLayout)?.let {
                for (comment in comments) {
                    it.addView(WidgetUtils.createHorizontalLine(this, 1))
                    _addCommentPropertyView(it, R.string.relevance, SectorComment.QUALITY_MAP, comment.qualityId)
                    _addCommentTextView(it, comment.text.orEmpty())
                }
                it.addView(WidgetUtils.createHorizontalLine(this, 1))
            }
        } else {
            showNoCommentToast()
        }
    }

    protected fun showRockComments(rockId: Int) {
        val comments = db.getRockComments(rockId)
        if (comments.isNotEmpty()) {
            _prepareCommentDialog().findViewById<LinearLayout>(R.id.commentLayout)?.let {
                for (comment in comments) {
                    it.addView(WidgetUtils.createHorizontalLine(this, 1))
                    _addCommentPropertyView(it, R.string.nature, RockComment.QUALITY_MAP, comment.qualityId)
                    _addCommentTextView(it, comment.text.orEmpty())
                }
                it.addView(WidgetUtils.createHorizontalLine(this, 1))
            }
        } else {
            showNoCommentToast()
        }
    }

    protected fun showRouteComments(routeId: Int) {
        val comments = db.getRouteComments(routeId)
        if (comments.isNotEmpty()) {
            _prepareCommentDialog().findViewById<LinearLayout>(R.id.commentLayout)?.let { it ->
                for (comment in comments) {
                    it.addView(WidgetUtils.createHorizontalLine(this, 1))
                    _addCommentPropertyView(it, R.string.route_quality, RouteComment.QUALITY_MAP, comment.qualityId)
                    _addCommentPropertyView(it, R.string.grade, RouteComment.GRADE_MAP, comment.gradeId)
                    _addCommentPropertyView(it, R.string.protection, RouteComment.SECURITY_MAP, comment.securityId)
                    _addCommentPropertyView(it, R.string.drying, RouteComment.WETNESS_MAP, comment.wetnessId)
                    _addCommentTextView(it, comment.text.orEmpty())
                }
                it.addView(WidgetUtils.createHorizontalLine(this, 1))
            }
        } else {
            showNoCommentToast()
        }
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

    private fun _addCommentPropertyView(layout: LinearLayout, titleRes: Int, propertyMap: Map<Int, String>, propertyKey: Int) {
        if (propertyMap.containsKey(propertyKey)) {
            _addCommentTextView(layout, getString(titleRes), propertyMap[propertyKey].orEmpty())
        }
    }

    private fun _addCommentTextView(layout: LinearLayout, textLeft: String, textRight: String = "") {
        layout.addView(WidgetUtils.createCommonRowLayout(this,
            textLeft = textLeft,
            textRight = textRight,
            textSizeDp = WidgetUtils.textFontSizeDp,
            typeface = Typeface.NORMAL))
    }

    abstract fun displayContent()
}
