/*
 * Copyright (C) 2019, 2022 Axel Paetzold
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
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.activity_properties.AscentFilterable
import com.yacgroup.yacguide.activity_properties.RockSearchable
import com.yacgroup.yacguide.activity_properties.RouteSearchable
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.list_adapters.RockViewAdapter
import com.yacgroup.yacguide.database.comment.RockComment
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.SearchBarHandler

class RockActivity : TableActivityWithOptionsMenu() {

    private lateinit var _viewAdapter: RockViewAdapter
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
                getAll = { db.getRocksForCountry(activityLevel.parentName) },
                getByName = { db.getRocksByNameForCountry(activityLevel.parentName, _filterName) },
                getByRelevance = { db.getRocksByRelevanceForCountry(activityLevel.parentName, _filterMaxRelevanceId) }
            ),
            ClimbingObjectLevel.eSector to RockGetter(
                getAll = { db.getRocksForRegion(activityLevel.parentId) },
                getByName = { db.getRocksByNameForRegion(activityLevel.parentId, _filterName) },
                getByRelevance = { db.getRocksByRelevanceForRegion(activityLevel.parentId, _filterMaxRelevanceId) }
            ),
            ClimbingObjectLevel.eRock to RockGetter(
                getAll = { db.getRocksForSector(activityLevel.parentId) },
                getByName = { db.getRocksByNameForSector(activityLevel.parentId, _filterName) },
                getByRelevance = { db.getRocksByRelevanceForSector(activityLevel.parentId, _filterMaxRelevanceId) }
            )
        )

        _searchBarHandler = SearchBarHandler(
            findViewById(R.id.searchBarLayout),
            R.string.rock_search,
            getString(R.string.only_official_summits),
            resources.getBoolean(R.bool.only_official_summits),
            customSettings,
            { onlyOfficialSummits -> _onlyOfficialSummits = onlyOfficialSummits },
            { rockNamePart, onlyOfficialSummits -> _onSearchBarUpdate(rockNamePart, onlyOfficialSummits) })

        _viewAdapter = RockViewAdapter(
            this,
            customSettings,
            activityLevel.level,
            db) { rockId, rockName ->
                _onRockSelected(rockId, rockName
            ) }
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter
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

    private fun _onRockSelected(rockId: Int, rockName: String) {
        startActivity(Intent(this@RockActivity, RouteActivity::class.java).apply {
            putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eRoute.value)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, rockId)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, rockName)
        })
    }
}

class RockGetter(
    val getAll: () -> List<Rock>,
    val getByName: () -> List<Rock>,
    val getByRelevance: () -> List<Rock>
)
