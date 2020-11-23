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
import android.widget.*

import com.yacgroup.yacguide.database.Comment.RegionComment
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.network.SectorParser
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.WidgetUtils

class SectorActivity : UpdatableTableActivity() {

    private lateinit var _region: Region

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val regionId = intent.getIntExtra(IntentConstants.REGION_KEY, DatabaseWrapper.INVALID_ID)

        jsonParser = SectorParser(db, this, regionId)
        _region = db.getRegion(regionId)!!
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_sector
    }

    override fun showComments(v: View) {
        val dialog = prepareCommentDialog()

        val layout = dialog.findViewById<View>(R.id.commentLayout) as LinearLayout
        val comments = db.getRegionsComments(_region.id)
        for (comment in comments) {
            val qualityId = comment.qualityId

            layout.addView(WidgetUtils.createHorizontalLine(this, 5))
            if (RegionComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        textLeft = getString(R.string.relevance),
                        textRight = RegionComment.QUALITY_MAP[qualityId].orEmpty(),
                        textSizeDp = WidgetUtils.textFontSizeDp,
                        typeface = Typeface.NORMAL))
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = comment.text.orEmpty(),
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    typeface = Typeface.NORMAL))
        }
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        this.title = _region.name
        val sectors = db.getSectors(_region.id)
        for (sector in sectors) {
            val sectorName = ParserUtils.decodeObjectNames(sector.name)
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@SectorActivity, RockActivity::class.java)
                intent.putExtra(IntentConstants.SECTOR_KEY, sector.id)
                startActivity(intent)
            }

            val rocks = db.getRocksForSector(sector.id, null)
            val rockCountString = _generateRockCountString(rocks)

            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = sectorName.first,
                    textRight = rockCountString,
                    onClickListener = onClickListener))
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = sectorName.second,
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    onClickListener = onClickListener))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
        if (sectors.isEmpty()) {
            displayDownloadButton()
        }
    }

    override fun deleteContent() {
        db.deleteSectorsRecursively(_region.id)
    }

    override fun searchRocks(rockName: String): List<Rock> {
        return db.getRocksForRegion(_region.id).filter { rock -> rock.name!!.toLowerCase().contains(rockName.toLowerCase()) }
    }

    private fun _generateRockCountString(rocks: List<Rock>): String {
        val countSummits = customSettings.getBoolean(getString(R.string.count_summits), resources.getBoolean(R.bool.count_summits))
        val countMassifs = customSettings.getBoolean(getString(R.string.count_massifs), resources.getBoolean(R.bool.count_massifs))
        val countBoulders = customSettings.getBoolean(getString(R.string.count_boulders), resources.getBoolean(R.bool.count_boulders))
        val countCaves = customSettings.getBoolean(getString(R.string.count_caves), resources.getBoolean(R.bool.count_caves))
        if (!(countSummits || countMassifs || countBoulders || countCaves)) {
            return ""
        }

        var filteredRocks = rocks
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
        if (!customSettings.getBoolean(getString(R.string.count_collapsed_rocks), resources.getBoolean(R.bool.count_collapsed_rocks))) {
            filteredRocks = filteredRocks.filter { it.status != Rock.statusCollapsed }
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
