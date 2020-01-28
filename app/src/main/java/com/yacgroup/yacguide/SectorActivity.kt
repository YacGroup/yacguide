/*
 * Copyright (C) 2018 Axel Pätzold
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

import com.yacgroup.yacguide.database.AppDatabase
import com.yacgroup.yacguide.database.Comment.RegionComment
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.network.SectorParser
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

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

            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    sectorName.orEmpty(),
                    "",
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
}
