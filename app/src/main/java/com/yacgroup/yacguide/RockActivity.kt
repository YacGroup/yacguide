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

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.activity_properties.AscentFilterable
import com.yacgroup.yacguide.activity_properties.RockSearchable
import com.yacgroup.yacguide.activity_properties.RouteSearchable
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.comment.RockComment
import com.yacgroup.yacguide.list_adapters.ItemDiffCallback
import com.yacgroup.yacguide.list_adapters.ListItem
import com.yacgroup.yacguide.list_adapters.ListViewAdapter
import com.yacgroup.yacguide.utils.*

class RockActivity : TableActivityWithOptionsMenu() {

    private lateinit var _viewAdapter: ListViewAdapter<Rock>
    private lateinit var _searchBarHandler: SearchBarHandler
    private lateinit var _rockGettersMap: Map<ClimbingObjectLevel, RockGetter>
    private var _onlyOfficialSummits: Boolean = false
    private var _rockNamePart: String = ""
    private var _filterName: String = ""
    private var _filterMaxRelevanceId: Int = RockComment.NO_INFO_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _filterName = intent.getStringExtra(IntentConstants.FILTER_NAME).orEmpty()
        _filterMaxRelevanceId = intent.getIntExtra(IntentConstants.FILTER_RELEVANCE, RockComment.NO_INFO_ID)

        properties = arrayListOf(
            RouteSearchable(this),
            RockSearchable(this),
            AscentFilterable(this)
        )

        _rockGettersMap = mapOf(
            ClimbingObjectLevel.eCountry to RockGetter(
                getAll = { db.getRocks() },
                getByName = { db.getRocksByName(_filterName) },
                getByRelevance = { db.getRocksByRelevance(_filterMaxRelevanceId) }
            ),
            ClimbingObjectLevel.eRegion to RockGetter(
                getAll = { db.getRocksForCountry(activityLevel.parentUId.name) },
                getByName = { db.getRocksByNameForCountry(activityLevel.parentUId.name, _filterName) },
                getByRelevance = { db.getRocksByRelevanceForCountry(activityLevel.parentUId.name, _filterMaxRelevanceId) }
            ),
            ClimbingObjectLevel.eSector to RockGetter(
                getAll = { db.getRocksForRegion(activityLevel.parentUId.id) },
                getByName = { db.getRocksByNameForRegion(activityLevel.parentUId.id, _filterName) },
                getByRelevance = { db.getRocksByRelevanceForRegion(activityLevel.parentUId.id, _filterMaxRelevanceId) }
            ),
            ClimbingObjectLevel.eRock to RockGetter(
                getAll = { db.getRocksForSector(activityLevel.parentUId.id) },
                getByName = { db.getRocksByNameForSector(activityLevel.parentUId.id, _filterName) },
                getByRelevance = { db.getRocksByRelevanceForSector(activityLevel.parentUId.id, _filterMaxRelevanceId) }
            )
        )

        _searchBarHandler = SearchBarHandler(
            searchBarLayout = findViewById(R.id.searchBarLayout),
            searchHintResource = R.string.rock_search,
            checkBoxTitle = getString(R.string.only_official_summits),
            checkBoxDefaultValue = resources.getBoolean(R.bool.only_official_summits),
            _settings = customSettings,
            initCallback = { onlyOfficialSummits -> _onlyOfficialSummits = onlyOfficialSummits },
            updateCallback = { rockNamePart, onlyOfficialSummits -> _onSearchBarUpdate(rockNamePart, onlyOfficialSummits) }
        )

        _viewAdapter = ListViewAdapter(ItemDiffCallback(
            _areItemsTheSame = { rock1, rock2 -> rock1.id == rock2.id },
            _areContentsTheSame = { rock1, rock2 -> rock1 == rock2 }
        )) { rock -> ListItem(
            backgroundColor = _getRockBackground(rock),
            typeface = _getRockTypeface(rock),
            titleText = _getRockSectorInfo(rock),
            mainText = _getRockMainText(rock),
            subText = ParserUtils.decodeObjectNames(rock.name).second,
            onClick = { _onRockSelected(rock) })
        }
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter
    }

    override fun getLayoutId() = R.layout.activity_rock

    override fun showComments(v: View) {
        when (activityLevel.level) {
            ClimbingObjectLevel.eSector -> showRegionComments(activityLevel.parentUId.id)
            ClimbingObjectLevel.eRock -> showSectorComments(activityLevel.parentUId.id)
            else -> showNoCommentToast()
        }
    }

    override fun displayContent() {
        val levelName = ParserUtils.decodeObjectNames(activityLevel.parentUId.name)
        this.title = if (levelName.first.isNotEmpty()) levelName.first else levelName.second

        var rocks = _getAndFilterRocks()
        // additional searchbar filters:
        if (_onlyOfficialSummits) {
            rocks = rocks.filter { _rockIsAnOfficialSummit(it) }
        }
        if (_rockNamePart.isNotEmpty()) {
            rocks = rocks.filter{ it.name.orEmpty().lowercase().contains(_rockNamePart.lowercase()) }
        }
        _viewAdapter.submitList(rocks)
    }

    override fun onStop() {
        _searchBarHandler.storeCustomSettings(getString(R.string.only_official_summits))
        super.onStop()
    }

    private fun _getAndFilterRocks(): List<Rock> {
        val rockGetter = _rockGettersMap[activityLevel.level]!!
        return if (_filterName.isEmpty() && _filterMaxRelevanceId == 0) {
            rockGetter.getAll()
        } else {
            val nameFilteredRocks =
                if (_filterName.isEmpty()) null
                else rockGetter.getByName().toSet()
            val relevanceFilteredRocks =
                if (_filterMaxRelevanceId == RockComment.NO_INFO_ID) null
                else rockGetter.getByRelevance().toSet()
            listOfNotNull(nameFilteredRocks, relevanceFilteredRocks).reduce {
                    intersection, filteredRocks -> intersection.intersect(filteredRocks)
            }.toList()
        }
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

    private fun _getRockBackground(rock: Rock): Int {
        val defaultBgColor = if (rock.status == Rock.statusProhibited || rock.status == Rock.statusCollapsed)
                visualUtils.prohibitedBgColor
            else
                visualUtils.defaultBgColor
        return AscendStyle.deriveAscentColor(
            ascentBitMask = rock.ascendsBitMask,
            leadColor = visualUtils.leadBgColor,
            followColor = visualUtils.followBgColor,
            defaultColor = defaultBgColor)
    }

    private fun _getRockTypeface(rock: Rock): Int {
        return if (rock.status == Rock.statusProhibited || rock.status == Rock.statusCollapsed)
                Typeface.ITALIC
            else if (rock.type != Rock.typeSummit)
                Typeface.NORMAL
            else
                Typeface.BOLD
    }

    private fun _getRockMainText(rock: Rock): Pair<String, String> {
        val rockName = ParserUtils.decodeObjectNames(rock.name)
        val typeAdd = if (rock.type != Rock.typeSummit)
                "  (${rock.type})"
            else
                ""
        val decorationAdd = AscendStyle.deriveAscentDecoration(
            rock.ascendsBitMask,
            visualUtils.botchIcon,
            visualUtils.projectIcon,
            visualUtils.watchingIcon)
        return Pair("${rock.nr}  ${rockName.first}$typeAdd$decorationAdd", rock.status.toString())
    }

    private fun _getRockSectorInfo(rock: Rock): Pair<String, String>? {
        var sectorInfo = ""
        if (activityLevel.level < ClimbingObjectLevel.eRock) {
            val sector = db.getSector(rock.parentId)!!
            if (activityLevel.level < ClimbingObjectLevel.eSector) {
                val region = db.getRegion(sector.parentId)!!
                sectorInfo = "${region.name} ${visualUtils.arrow} "
            }
            val sectorNames = ParserUtils.decodeObjectNames(sector.name)
            sectorInfo += sectorNames.first
            if (sectorNames.second.isNotEmpty()) {
                sectorInfo += " / ${sectorNames.second}"
            }
        }
        return if (sectorInfo.isNotEmpty())
                Pair(sectorInfo, "")
            else
                null
    }

    private fun _onRockSelected(rock: Rock) {
        startActivity(Intent(this@RockActivity, RouteActivity::class.java).apply {
            putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eRoute.value)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, rock.id)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, rock.name)
        })
    }
}

class RockGetter(
    val getAll: () -> List<Rock>,
    val getByName: () -> List<Rock>,
    val getByRelevance: () -> List<Rock>
)
