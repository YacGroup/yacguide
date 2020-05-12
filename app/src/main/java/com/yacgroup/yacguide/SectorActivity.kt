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

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout

import com.yacgroup.yacguide.database.AppDatabase
import com.yacgroup.yacguide.database.Comment.RegionComment
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.network.SectorParser
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils
import kotlin.Result

class SectorActivity : UpdatableTableActivity() {

    private var _region: Region? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val regionId = intent.getIntExtra(IntentConstants.REGION_KEY, AppDatabase.INVALID_ID)

        jsonParser = SectorParser(db, this, regionId)
        _region = db.regionDao().getRegion(regionId)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_sector
    }

    override fun showComments(v: View) {
        val dialog = prepareCommentDialog()

        val layout = dialog.findViewById<View>(R.id.commentLayout) as LinearLayout
        val comments = _region?.let { db.regionCommentDao().getAll(it.id) } ?: emptyArray()
        for (comment in comments) {
            val qualityId = comment.qualityId

            layout.addView(WidgetUtils.createHorizontalLine(this, 5))
            if (RegionComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Bedeutung:",
                        RegionComment.QUALITY_MAP[qualityId].orEmpty(),
                        WidgetUtils.textFontSizeDp,
                        View.OnClickListener { },
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    comment.text.orEmpty(),
                    "",
                    WidgetUtils.textFontSizeDp,
                    View.OnClickListener { },
                    Color.WHITE,
                    Typeface.NORMAL,
                    10, 10, 10, 10))
        }
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        this.title = _region?.name.orEmpty()
        val sectors = _region?.let { db.sectorDao().getAll(it.id) } ?: emptyArray()
        for (sector in sectors) {
            val sectorName = sector.name
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@SectorActivity, RockActivity::class.java)
                intent.putExtra(IntentConstants.SECTOR_KEY, sector.id)
                startActivity(intent)
            }

            val rocks = db.rockDao().getAll(sector.id)
            val rockCountString = _generateRockCountString(rocks)

            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    sectorName.orEmpty(),
                    rockCountString,
                    WidgetUtils.tableFontSizeDp,
                    onClickListener,
                    Color.WHITE,
                    Typeface.BOLD))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    override fun deleteContent() {
        _region?.let { db.deleteSectors(it.id) }
    }

    private fun _generateRockCountString(rocks: Array<Rock>): String {
        val countSummits = customSettings.getBoolean(getString(R.string.count_summits), resources.getBoolean(R.bool.count_summits))
        val countMassifs = customSettings.getBoolean(getString(R.string.count_massifs), resources.getBoolean(R.bool.count_massifs))
        val countBoulders = customSettings.getBoolean(getString(R.string.count_boulders), resources.getBoolean(R.bool.count_boulders))
        val countCaves = customSettings.getBoolean(getString(R.string.count_caves), resources.getBoolean(R.bool.count_caves))
        if (!(countSummits || countMassifs || countBoulders || countCaves)) {
            return ""
        }

        var filteredRocks = rocks.toList()
        if (!countSummits) {
            filteredRocks = filteredRocks.filter{ it.type != Rock.typeSummit && it.type != Rock.typeAlpine }
        }
        if (!countMassifs) {
            filteredRocks = filteredRocks.filter { it.type != Rock.typeMassif && it.type != Rock.typeStonePit}
        }
        if (!countBoulders) {
            filteredRocks = filteredRocks.filter { it.type != Rock.typeBoulder }
        }
        if (!countCaves) {
            filteredRocks = filteredRocks.filter { it.type != Rock.typeCave }
        }

        if (!customSettings.getBoolean(getString(R.string.count_unofficial_rocks), resources.getBoolean(R.bool.count_unofficial_rocks))) {
            filteredRocks = filteredRocks.filter { it.type != Rock.typeUnofficial }
        }
        if (!customSettings.getBoolean(getString(R.string.count_prohibited_rocks), resources.getBoolean(R.bool.count_prohibited_rocks))) {
            filteredRocks = filteredRocks.filter { it.status != Rock.statusProhibited }
        }
        val allRockCount = filteredRocks.size

        filteredRocks = if (customSettings.getBoolean(getString(R.string.count_only_leads), resources.getBoolean(R.bool.count_only_leads))) {
            filteredRocks.filter { AscendStyle.isLead(it.ascendsBitMask) }
        } else {
            filteredRocks.filter { AscendStyle.isLead(it.ascendsBitMask) || AscendStyle.isFollow(it.ascendsBitMask) }
        }

        return "(${filteredRocks.size} / $allRockCount)"
    }
}
