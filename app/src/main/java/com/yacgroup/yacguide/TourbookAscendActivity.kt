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
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.database.*
import com.yacgroup.yacguide.databinding.ActivityTourbookAscendBinding
import com.yacgroup.yacguide.list_adapters.*
import com.yacgroup.yacguide.utils.*

class TourbookAscendActivity : BaseNavigationActivity<ActivityTourbookAscendBinding>() {

    private lateinit var _listView: RecyclerView
    private lateinit var _viewAdapter: ListViewAdapter<ListItem>
    private lateinit var _visualUtils: VisualUtils
    private lateinit var _db: DatabaseWrapper
    private lateinit var _ascent: Ascend

    override fun getViewBinding() = ActivityTourbookAscendBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.menu_tourbook)

        _db = DatabaseWrapper(this)

        _listView = activityViewBinding.layoutListViewContent.tableRecyclerView
        _viewAdapter = ListViewAdapter(ItemDiffCallback()) { item -> item }

        _visualUtils = VisualUtils(this)

        val ascentId = intent.getIntExtra(IntentConstants.ASCEND_ID, DatabaseWrapper.INVALID_ID)
        _ascent = _db.getAscend(ascentId)!!
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_delete, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        _ascent = _db.getAscend(_ascent.id)!!
        _displayContent()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> edit()
            R.id.action_delete -> delete()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun edit() {
        startActivity(Intent(this@TourbookAscendActivity, AscendActivity::class.java).apply {
            putExtra(IntentConstants.ASCEND_ID, _ascent.id)
        })
    }

    private fun delete() {
        DialogWidgetBuilder(this, R.string.dialog_question_delete_ascend).apply {
            setIcon(android.R.drawable.ic_dialog_alert)
            setNegativeButton()
            setPositiveButton { _, _ ->
                _db.deleteAscend(_ascent)
                Toast.makeText(this@TourbookAscendActivity, R.string.ascend_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
        }.show()
    }

    private fun _displayContent() {
        var route = _db.getRoute(_ascent.routeId)
        val rock: Rock
        val sector: Sector
        val region: Region
        if (route != null) {
            rock = _db.getRock(route.parentId) ?: _db.createUnknownRock()
            sector = _db.getSector(rock.parentId) ?: _db.createUnknownSector()
            region = _db.getRegion(sector.parentId) ?: _db.createUnknownRegion()
        } else {
            route = _db.createUnknownRoute()
            rock = _db.createUnknownRock()
            sector = _db.createUnknownSector()
            region = _db.createUnknownRegion()
            Toast.makeText(this, R.string.corresponding_route_not_found, Toast.LENGTH_LONG).show()
        }

        val partnerNames = _db.getPartnerNames(_ascent.partnerIds?.toList().orEmpty())
        val sectorName = ParserUtils.decodeObjectNames(sector.name)
        val rockName = ParserUtils.decodeObjectNames(rock.name)
        val routeName = ParserUtils.decodeObjectNames(route.name)

        _listView.adapter = _viewAdapter
        _viewAdapter.submitList(listOf(
                ListItem(
                    backgroundColor = _visualUtils.defaultBgColor,
                    mainText = Pair("${_ascent.day}.${_ascent.month}.${_ascent.year}", region.name.orEmpty()),
                    subText = null),
                ListItem(
                    backgroundColor = _visualUtils.defaultBgColor,
                    titleText = Pair(getString(R.string.sector), ""),
                    mainText = Pair(sectorName.first, sectorName.second),
                    subText = null),
                ListItem(
                    backgroundColor = _visualUtils.defaultBgColor,
                    titleText = Pair(getString(R.string.rock), ""),
                    mainText = Pair(rockName.first, rockName.second),
                    subText = null),
                ListItem(
                    backgroundColor = _visualUtils.defaultBgColor,
                    titleText = Pair(getString(R.string.route), ""),
                    mainText = Pair(routeName.first, routeName.second),
                    subText = null),
                ListItem(
                    backgroundColor = _visualUtils.defaultBgColor,
                    titleText = Pair(getString(R.string.grade), getString(R.string.style)),
                    mainText = Pair(route.grade.orEmpty(), AscendStyle.fromId(_ascent.styleId)?.styleName.orEmpty()),
                    subText = null),
                ListItem(
                    backgroundColor = _visualUtils.defaultBgColor,
                    titleText = Pair(getString(R.string.partner), ""),
                    mainText = Pair(partnerNames.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: " - ", ""),
                    subText = null),
                ListItem(
                    backgroundColor = _visualUtils.defaultBgColor,
                    titleText = Pair(getString(R.string.notes), ""),
                    mainText = Pair(_ascent.notes?.takeUnless { it.isBlank() } ?: " - ", ""),
                    subText = null)
            )
        )
    }

}
