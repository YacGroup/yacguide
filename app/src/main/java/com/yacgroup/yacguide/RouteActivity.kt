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
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.yacgroup.yacguide.database.comment.RockComment
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.WidgetUtils

class RouteActivity : TableActivity() {

    private var _rock: Rock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rockId = intent.getIntExtra(IntentConstants.ROCK_KEY, DatabaseWrapper.INVALID_ID)

        _rock = db.getRock(rockId)
        val rockStatus = when (_rock?.status){
            Rock.statusCollapsed -> getString(R.string.rock_collapsed)
            Rock.statusProhibited -> getString(R.string.rock_fully_prohibited)
            Rock.statusTemporarilyProhibited -> getString(R.string.rock_temporarily_prohibited)
            Rock.statusPartlyProhibited -> getString(R.string.rock_partly_prohibited)
            else -> {
                if (_rock?.type == Rock.typeUnofficial) {
                    getString(R.string.rock_inofficial)
                } else {
                    ""
                }
            }
        }
        findViewById<TextView>(R.id.infoTextView).text = rockStatus
    }

    fun showMap(v: View) {
        val gmmIntentUri = Uri.parse("geo:${_rock?.latitude},${_rock?.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        if (mapIntent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, R.string.no_map_app_available, Toast.LENGTH_SHORT).show()
        } else {
            startActivity(mapIntent)
        }
    }

    override fun showComments(v: View) {
        val dialog = prepareCommentDialog()

        val layout = dialog.findViewById<LinearLayout>(R.id.commentLayout)
        val comments = _rock?.let { db.getRockComments(it.id) } ?: emptyList()
        for (comment in comments) {
            val qualityId = comment.qualityId
            val text = comment.text

            layout.addView(WidgetUtils.createHorizontalLine(this, 5))
            if (RockComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        textLeft = getString(R.string.nature),
                        textRight = RockComment.QUALITY_MAP[qualityId].orEmpty(),
                        textSizeDp = WidgetUtils.textFontSizeDp,
                        typeface = Typeface.NORMAL))
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = text.orEmpty(),
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    typeface = Typeface.NORMAL))
        }
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        val rockName = ParserUtils.decodeObjectNames(_rock?.name)
        this.title = if (rockName.first.isNotEmpty()) rockName.first else rockName.second

        val routes = _rock?.let { db.getRoutes(it.id) } ?: emptyList()
        for (route in routes) {
            val routeName = ParserUtils.decodeObjectNames(route.name)
            val commentCount = db.getRouteCommentCount(route.id)
            val commentCountAdd = if (commentCount > 0) "   [$commentCount]" else ""
            val onCLickListener = View.OnClickListener {
                val intent = Intent(this@RouteActivity, DescriptionActivity::class.java)
                intent.putExtra(IntentConstants.ROUTE_KEY, route.id)
                startActivityForResult(intent, 0)
            }
            val statusId = route.statusId
            var typeface = Typeface.BOLD
            var bgColor = Color.WHITE
            if (statusId == 3) { // prohibited
                typeface = Typeface.ITALIC
                bgColor = Color.LTGRAY
            }
            bgColor = colorizeEntry(route.ascendsBitMask, bgColor)
            val decorationAdd = decorateEntry(route.ascendsBitMask)
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = "${routeName.first}$commentCountAdd$decorationAdd",
                    textRight = route.grade.orEmpty(),
                    onClickListener = onCLickListener,
                    bgColor = bgColor,
                    typeface = typeface))
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = routeName.second,
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    onClickListener = onCLickListener,
                    bgColor = bgColor,
                    typeface = typeface))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_route
    }
}

