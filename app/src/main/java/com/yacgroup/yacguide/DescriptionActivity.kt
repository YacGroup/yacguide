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
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.yacgroup.yacguide.database.comment.RouteComment
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.utils.DateUtils
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
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
        val comments = _route?.let { db.getRouteComments(it.id) } ?: emptyList()
        if (comments.isNotEmpty()) {
            prepareCommentDialog().findViewById<LinearLayout>(R.id.commentLayout)?.let { it ->
                for ((idx, comment) in comments.withIndex()) {
                    if (idx > 0) {
                        it.addView(WidgetUtils.createHorizontalLine(this, 1))
                    }
                    if (RouteComment.QUALITY_MAP.containsKey(comment.qualityId)) {
                        it.addView(WidgetUtils.createCommonRowLayout(this,
                                textLeft = getString(R.string.route_quality),
                                textRight = RouteComment.QUALITY_MAP[comment.qualityId].orEmpty(),
                                textSizeDp = WidgetUtils.textFontSizeDp,
                                typeface = Typeface.NORMAL))
                    }
                    if (RouteComment.GRADE_MAP.containsKey(comment.gradeId)) {
                        it.addView(WidgetUtils.createCommonRowLayout(this,
                                textLeft = getString(R.string.grade),
                                textRight = RouteComment.GRADE_MAP[comment.gradeId].orEmpty(),
                                textSizeDp = WidgetUtils.textFontSizeDp,
                                typeface = Typeface.NORMAL))
                    }
                    if (RouteComment.SECURITY_MAP.containsKey(comment.securityId)) {
                        it.addView(WidgetUtils.createCommonRowLayout(this,
                                textLeft = getString(R.string.protection),
                                textRight = RouteComment.SECURITY_MAP[comment.securityId].orEmpty(),
                                textSizeDp = WidgetUtils.textFontSizeDp,
                                typeface = Typeface.NORMAL))
                    }
                    if (RouteComment.WETNESS_MAP.containsKey(comment.wetnessId)) {
                        it.addView(WidgetUtils.createCommonRowLayout(this,
                                textLeft = getString(R.string.drying),
                                textRight = RouteComment.WETNESS_MAP[comment.wetnessId].orEmpty(),
                                textSizeDp = WidgetUtils.textFontSizeDp,
                                typeface = Typeface.NORMAL))
                    }

                    it.addView(WidgetUtils.createCommonRowLayout(this,
                            textLeft = comment.text.orEmpty(),
                            textSizeDp = WidgetUtils.textFontSizeDp,
                            typeface = Typeface.NORMAL))
                }
            }
        } else {
            showNoCommentToast()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun enterAscend(v: View) {
        _route?.let {
            val intent = Intent(this@DescriptionActivity, AscendActivity::class.java)
            intent.putExtra(IntentConstants.ROUTE_KEY, it.id)
            startActivityForResult(intent, 0)
        }

    }

    @Suppress("UNUSED_PARAMETER")
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
        val routeName = ParserUtils.decodeObjectNames(_route?.name)
        this.title = "${if (routeName.first.isNotEmpty()) routeName.first else routeName.second}   ${_route?.grade.orEmpty()}"

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
                textLeft = firstAscendClimbers,
                textRight = firstAscendDate,
                textSizeDp = WidgetUtils.infoFontSizeDp))

        _route?.typeOfClimbing?.takeIf { it.isNotEmpty() }?.let {
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = getString(R.string.type_of_climbing),
                    textSizeDp = WidgetUtils.infoFontSizeDp,
                    padding = WidgetUtils.Padding(40, 20, 20, 0)))
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = it,
                    textSizeDp = WidgetUtils.infoFontSizeDp,
                    typeface = Typeface.NORMAL,
                    padding = WidgetUtils.Padding(40, 0, 20, 0)))
        }

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = _route?.description.orEmpty()))
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_description
    }
}
