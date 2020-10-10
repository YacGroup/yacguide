/*
 * Copyright (C) 2020 Axel Pätzold
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
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.utils.AscendStyle

import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.WidgetUtils

class SelectedRockActivity : TableActivity() {

    private var _rockIds: ArrayList<Int> = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _rockIds = intent.getIntegerArrayListExtra(IntentConstants.SELECTED_ROCK_IDS)
        displayContent()
    }

    override fun displayContent() {
        this.title = getString(R.string.title_rock_search_results)
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        for (rockId in _rockIds) {
            val rock = db.getRock(rockId)!!
            val rockName = ParserUtils.decodeObjectNames(rock.name)
            var bgColor = Color.WHITE
            var typeface = Typeface.BOLD
            var typeAdd = ""
            if (rock.type != Rock.typeSummit) {
                typeface = Typeface.NORMAL
                typeAdd = "  (${rock.type})"
            }
            val botchAdd = if (AscendStyle.isBotch(rock.ascendsBitMask)) getString(R.string.botch) else ""
            val projectAdd = if (AscendStyle.isProject(rock.ascendsBitMask)) getString(R.string.project) else ""
            val watchingAdd = if (AscendStyle.isWatching(rock.ascendsBitMask)) getString(R.string.watching) else ""
            if (rock.status == Rock.statusProhibited || rock.status == Rock.statusCollapsed) {
                typeface = Typeface.ITALIC
                bgColor = Color.LTGRAY
            }
            if (AscendStyle.isLead(rock.ascendsBitMask)) {
                bgColor = customSettings.getInt(getString(R.string.lead), ContextCompat.getColor(this, R.color.color_lead))
            } else if (AscendStyle.isFollow(rock.ascendsBitMask)) {
                bgColor = customSettings.getInt(getString(R.string.follow), ContextCompat.getColor(this, R.color.color_follow))
            }
            val onClickListener = View.OnClickListener {
                val intent = Intent(this, RouteActivity::class.java)
                intent.putExtra(IntentConstants.ROCK_KEY, rockId)
                startActivityForResult(intent, 0)
            }
            val sector = db.getSector(rock.parentId)!!
            val sectorName = ParserUtils.decodeObjectNames(sector.name)
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = db.getRegion(sector.parentId)?.name.orEmpty(),
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    onClickListener = onClickListener,
                    bgColor = bgColor,
                    typeface = Typeface.NORMAL))
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = sectorName.first,
                    textRight = sectorName.second,
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    onClickListener = onClickListener,
                    bgColor = bgColor))
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = "${rockName.first}$typeAdd$botchAdd$projectAdd$watchingAdd",
                    textRight = rockName.second,
                    onClickListener = onClickListener,
                    bgColor = bgColor,
                    typeface = typeface))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_selected_rocks
    }
}
