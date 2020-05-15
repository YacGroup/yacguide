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

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.yacgroup.yacguide.database.Comment.RouteComment
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.utils.DateUtils
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

class DescriptionActivity : TableActivity() {

    private var _route: Route? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routeId = intent.getIntExtra(IntentConstants.ROUTE_KEY, DatabaseWrapper.INVALID_ID)

        _route = db.getRoute(routeId)
        val routeStatusId = _route?.statusId ?: 0
        if (routeStatusId > 1) {
            findViewById<TextView>(R.id.infoTextView).text =
                    "Achtung: Der Weg ist ${Route.STATUS[routeStatusId]}"
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _route = db.getRoute(_route!!.id) // update route instance

            val ascendsButton = findViewById<ImageButton>(R.id.ascendsButton)
            ascendsButton.visibility = if (_route!!.ascendsBitMask > 0) View.VISIBLE else View.INVISIBLE
            Toast.makeText(this, R.string.ascends_refreshed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun showComments(v: View) {
        val dialog = prepareCommentDialog()

        val layout = dialog.findViewById<LinearLayout>(R.id.commentLayout)
        val comments = _route?.let { db.getRouteComments(it.id) } ?: emptyList()
        for (comment in comments) {
            val qualityId = comment.qualityId
            val gradeId = comment.gradeId
            val securityId = comment.securityId
            val wetnessId = comment.wetnessId
            val text = comment.text

            layout.addView(WidgetUtils.createHorizontalLine(this, 5))
            if (RouteComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        getString(R.string.route_quality),
                        RouteComment.QUALITY_MAP[qualityId].orEmpty(),
                        WidgetUtils.textFontSizeDp,
                        View.OnClickListener { },
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }
            if (RouteComment.GRADE_MAP.containsKey(gradeId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        getString(R.string.grade),
                        RouteComment.GRADE_MAP[gradeId].orEmpty(),
                        WidgetUtils.textFontSizeDp,
                        View.OnClickListener { },
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }
            if (RouteComment.SECURITY_MAP.containsKey(securityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        getString(R.string.protection),
                        RouteComment.SECURITY_MAP[securityId].orEmpty(),
                        WidgetUtils.textFontSizeDp,
                        View.OnClickListener { },
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }
            if (RouteComment.WETNESS_MAP.containsKey(wetnessId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        getString(R.string.drying),
                        RouteComment.WETNESS_MAP[wetnessId].orEmpty(),
                        WidgetUtils.textFontSizeDp,
                        View.OnClickListener { },
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }

            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    text.orEmpty(),
                    "",
                    WidgetUtils.textFontSizeDp,
                    View.OnClickListener { },
                    Color.WHITE,
                    Typeface.NORMAL,
                    10, 10, 10, 10))
        }
    }

    fun enterAscend(v: View) {
        _route?.let {
            val intent = Intent(this@DescriptionActivity, AscendActivity::class.java)
            intent.putExtra(IntentConstants.ROUTE_KEY, it.id)
            startActivityForResult(intent, 0)
        }

    }

    fun goToAscends(v: View) {
        _route?.let {
            val intent = Intent(this@DescriptionActivity, TourbookAscendActivity::class.java)
            intent.putExtra(IntentConstants.ROUTE_KEY, it.id)
            startActivityForResult(intent, 0)
        }
    }

    override fun displayContent() {
        findViewById<View>(R.id.ascendsButton).visibility = if (_route!!.ascendsBitMask > 0) View.VISIBLE else View.INVISIBLE

        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        this.title = "${_route?.name.orEmpty()}   ${_route?.grade.orEmpty()}"

        var firstAscendClimbers = _route
                ?.firstAscendLeader
                ?.takeUnless { it.isEmpty() }
                ?: getString(R.string.first_ascend_unknown)

        firstAscendClimbers += _route
                ?.firstAscendFollower
                ?.takeUnless { it.isEmpty() }
                ?.let { ", $it" }
                ?: ""

        val firstAscendDate = _route
                ?.firstAscendDate
                ?.takeUnless { it == DateUtils.UNKNOWN_DATE }
                ?.let { DateUtils.formatDate(it) }
                ?: getString(R.string.date_unknown)


        layout.addView(WidgetUtils.createCommonRowLayout(this,
                firstAscendClimbers,
                firstAscendDate,
                WidgetUtils.infoFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.BOLD,
                20, 20, 20, 0))

        _route?.typeOfClimbing?.takeIf { it.isNotEmpty() }?.let {
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    getString(R.string.type_of_climbing),
                    it,
                    WidgetUtils.infoFontSizeDp,
                    View.OnClickListener { },
                    Color.WHITE,
                    Typeface.NORMAL,
                    20, 20, 20, 0))
        }

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                _route?.description.orEmpty(),
                "",
                WidgetUtils.tableFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.BOLD))
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_description
    }
}
