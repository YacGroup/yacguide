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

import com.yacgroup.yacguide.database.comment.SectorComment
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Sector
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.SearchBarHandler
import com.yacgroup.yacguide.utils.WidgetUtils

class RockActivity : TableActivityWithOptionsMenu() {

    private lateinit var _sector: Sector
    private lateinit var _searchBarHandler: SearchBarHandler
    private var _onlyOfficialSummits: Boolean = false
    private var _rockNamePart: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sectorId = intent.getIntExtra(IntentConstants.SECTOR_KEY, DatabaseWrapper.INVALID_ID)
        _sector = db.getSector(sectorId)!!

        val rockSearchable = RockSearchable(
            this
        ) { db.getRocksForSector(sectorId) }
        val ascentFilterable = AscentFilterable(
            this,
            { db.getProjectedRocksForSector(sectorId) },
            { db.getBotchedRocksForSector(sectorId) }
        )
        properties = arrayListOf(rockSearchable, ascentFilterable)

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
        val comments = db.getSectorComments(_sector.id)
        if (comments.isNotEmpty()) {
            prepareCommentDialog().findViewById<LinearLayout>(R.id.commentLayout)?.let {
                for ((idx, comment) in comments.withIndex()) {
                    val qualityId = comment.qualityId
                    val text = comment.text

                    if (idx > 0) {
                        it.addView(WidgetUtils.createHorizontalLine(this, 1))
                    }
                    if (SectorComment.QUALITY_MAP.containsKey(qualityId)) {
                        it.addView(WidgetUtils.createCommonRowLayout(this,
                                textLeft = getString(R.string.relevance),
                                textRight = SectorComment.QUALITY_MAP[qualityId].orEmpty(),
                                textSizeDp = WidgetUtils.textFontSizeDp,
                                typeface = Typeface.NORMAL))
                    }
                    it.addView(WidgetUtils.createCommonRowLayout(this,
                            textLeft = text.orEmpty(),
                            textSizeDp = WidgetUtils.textFontSizeDp,
                            typeface = Typeface.NORMAL))
                }
            }
        } else {
            showNoCommentToast()
        }
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        val sectorName = ParserUtils.decodeObjectNames(_sector.name)
        this.title = if (sectorName.first.isNotEmpty()) sectorName.first else sectorName.second

        var rocks = db.getRocksForSector(_sector.id)
        if (_onlyOfficialSummits) {
            rocks = rocks.filter { _rockIsAnOfficialSummit(it) }
        }
        for (rock in rocks) {
            val rockName = ParserUtils.decodeObjectNames(rock.name)
            if (_rockNamePart.isNotEmpty() && rockName.toList().none { it.lowercase().contains(_rockNamePart.lowercase()) }) {
                continue
            }
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
                intent.putExtra(IntentConstants.ROCK_KEY, rock.id)
                startActivityForResult(intent, 0)
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
