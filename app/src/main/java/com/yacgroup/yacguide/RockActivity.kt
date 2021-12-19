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
import android.widget.*
import com.yacgroup.yacguide.activity_properties.AscentFilterable
import com.yacgroup.yacguide.activity_properties.RockSearchable
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.SearchBarHandler
import com.yacgroup.yacguide.utils.WidgetUtils

class RockActivity : TableActivityWithOptionsMenu() {

    private lateinit var _searchBarHandler: SearchBarHandler
    private var _onlyOfficialSummits: Boolean = false
    private var _rockNamePart: String = ""
    private var _filterName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _filterName = intent.getStringExtra(IntentConstants.FILTER_NAME).orEmpty()

        properties = arrayListOf(RockSearchable(this), AscentFilterable(this))

        _searchBarHandler = SearchBarHandler(
            findViewById(R.id.searchBarLayout),
            R.string.rock_search,
            getString(R.string.only_official_summits),
            resources.getBoolean(R.bool.only_official_summits),
            customSettings
        ) { rockNamePart, onlyOfficialSummits -> _onSearchBarUpdate(rockNamePart, onlyOfficialSummits) }
    }

    override fun getLayoutId() = R.layout.activity_rock

    override fun showComments(v: View) {
        when (activityLevel.level) {
            ClimbingObjectLevel.eSector -> showRegionComments(activityLevel.parentId)
            ClimbingObjectLevel.eRock -> showSectorComments(activityLevel.parentId)
            else -> showNoCommentToast()
        }
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()

        val levelName = ParserUtils.decodeObjectNames(activityLevel.parentName)
        this.title = if (levelName.first.isNotEmpty()) levelName.first else levelName.second

        var rocks = _getAndFilterRocks()
        // additional searchbar filters:
        if (_onlyOfficialSummits) {
            rocks = rocks.filter { _rockIsAnOfficialSummit(it) }
        }
        if (_rockNamePart.isNotEmpty()) {
            rocks = rocks.filter{ it.name.orEmpty().lowercase().contains(_rockNamePart.lowercase()) }
        }

        for (rock in rocks) {
            val rockName = ParserUtils.decodeObjectNames(rock.name)
            var bgColor = Color.WHITE
            var typeface = Typeface.BOLD
            var typeAdd = ""
            if (rock.type != Rock.typeSummit) {
                typeface = Typeface.NORMAL
                typeAdd = "  (${rock.type})"
            }
            if (rock.status == Rock.statusProhibited || rock.status == Rock.statusCollapsed) {
                typeface = Typeface.ITALIC
                bgColor = Color.LTGRAY
            }
            bgColor = colorizeEntry(rock.ascendsBitMask, bgColor)
            val decorationAdd = decorateEntry(rock.ascendsBitMask)
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@RockActivity, RouteActivity::class.java)
                intent.putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eRoute.value)
                intent.putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, rock.id)
                intent.putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, rock.name)
                startActivityForResult(intent, 0)
            }
            val sectorInfo = _getSectorInfo(rock)
            if (sectorInfo.isNotEmpty()) {
                layout.addView(
                    WidgetUtils.createCommonRowLayout(
                        this,
                        textLeft = sectorInfo,
                        textSizeDp = WidgetUtils.textFontSizeDp,
                        onClickListener = onClickListener,
                        bgColor = bgColor,
                        typeface = Typeface.NORMAL
                    )
                )
            }
            layout.addView(
                WidgetUtils.createCommonRowLayout(
                    this,
                    textLeft = "${rock.nr}  ${rockName.first}$typeAdd$decorationAdd",
                    textRight = rock.status.toString(),
                    onClickListener = onClickListener,
                    bgColor = bgColor,
                    typeface = typeface
                )
            )
            layout.addView(
                WidgetUtils.createCommonRowLayout(
                    this,
                    textLeft = rockName.second,
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    onClickListener = onClickListener,
                    bgColor = bgColor,
                    typeface = typeface
                )
            )
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    override fun onStop() {
        _searchBarHandler.storeCustomSettings(getString(R.string.only_official_summits))
        super.onStop()
    }

    private fun _getAndFilterRocks(): List<Rock> {
        val rocks = when (activityLevel.level) {
            ClimbingObjectLevel.eCountry -> db.getRocks()
            ClimbingObjectLevel.eRegion -> db.getRocksForCountry(activityLevel.parentName)
            ClimbingObjectLevel.eSector -> db.getRocksForRegion(activityLevel.parentId)
            ClimbingObjectLevel.eRock -> db.getRocksForSector(activityLevel.parentId)
            else -> emptyList()
        }
        return if (_filterName.isNotEmpty())
                   rocks.filter { it.name.orEmpty().lowercase().contains(_filterName.lowercase()) }
               else
                   rocks
    }

    private fun _getSectorInfo(rock: Rock): String {
        var sectorInfo = ""
        if (activityLevel.level.value < ClimbingObjectLevel.eRock.value) {
            val sector = db.getSector(rock.parentId)!!
            if (activityLevel.level.value < ClimbingObjectLevel.eSector.value) {
                val region = db.getRegion(sector.parentId)!!
                sectorInfo = "${region.name} ${getString(R.string.right_arrow)} "
            }
            val sectorNames = ParserUtils.decodeObjectNames(sector.name)
            sectorInfo += sectorNames.first
            if (sectorNames.second.isNotEmpty()) {
                sectorInfo += " / ${sectorNames.second}"
            }
        }
        return sectorInfo
    }

    private fun _onSearchBarUpdate(rockNamePart: String, onlyOfficialSummits: Boolean)
    {
        _rockNamePart = rockNamePart
        _onlyOfficialSummits = onlyOfficialSummits
        displayContent()
    }

    private fun _rockIsAnOfficialSummit(rock: Rock): Boolean {
        return (rock.type == Rock.typeSummit || rock.type == Rock.typeAlpine)
                && rock.status != Rock.statusProhibited
                && rock.status != Rock.statusCollapsed
    }
}
