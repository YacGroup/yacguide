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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.utils.*

class DescriptionActivity : TableActivity() {

    private lateinit var _route: Route

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _route = db.getRoute(activityLevel.parentId)!!
        val routeStatusId = _route.statusId
        if (routeStatusId > 1) {
            findViewById<TextView>(R.id.infoTextView).text =
                    "Achtung: Der Weg ist ${Route.STATUS[routeStatusId]}"
        }
    }

    override fun getLayoutId() = R.layout.activity_description

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _route = db.getRoute(activityLevel.parentId)!! // update route instance
            displayContent()
            Toast.makeText(this, R.string.ascends_refreshed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun showComments(v: View) {
        showRouteComments(activityLevel.parentId)
    }

    @Suppress("UNUSED_PARAMETER")
    fun enterAscend(v: View) {
        val intent = Intent(this@DescriptionActivity, AscendActivity::class.java)
        intent.putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, _route.id)
        startActivityForResult(intent, 0)
    }

    @Suppress("UNUSED_PARAMETER")
    fun goToAscends(v: View) {
        val intent = Intent(this@DescriptionActivity, TourbookAscendActivity::class.java)
        intent.putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, _route.id)
        startActivityForResult(intent, 0)
    }

    override fun displayContent() {
        findViewById<View>(R.id.ascendsButton).visibility =
                if (AscendStyle.hasAscendBit(_route.ascendsBitMask))
                    View.VISIBLE
                else
                    View.INVISIBLE

        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        val routeName = ParserUtils.decodeObjectNames(_route.name)
        this.title = "${if (routeName.first.isNotEmpty()) routeName.first else routeName.second}   ${_route.grade.orEmpty()}"

        var firstAscendClimbers = _route.firstAscendLeader
                ?.takeUnless { it.isEmpty() }
                ?: getString(R.string.first_ascend_unknown)

        firstAscendClimbers += _route.firstAscendFollower
                ?.takeUnless { it.isEmpty() }
                ?.let { ", $it" }
                ?: ""

        val firstAscendDate = _route.firstAscendDate
                ?.takeUnless { it == DateUtils.UNKNOWN_DATE }
                ?.let { DateUtils.formatDate(it) }
                ?: getString(R.string.date_unknown)

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = firstAscendClimbers,
                textRight = firstAscendDate,
                textSizeDp = WidgetUtils.infoFontSizeDp))

        _route.typeOfClimbing?.takeIf { it.isNotEmpty() }?.let {
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
                textLeft = _route.description.orEmpty()))
    }
}
