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

import com.yacgroup.yacguide.network.RegionParser
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

class RegionActivity : UpdatableTableActivity() {

    private var _countryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _countryName = intent.getStringExtra(IntentConstants.COUNTRY_KEY)
        jsonParser = RegionParser(db, this, _countryName!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_region
    }

    override fun displayContent() {
        this.title = _countryName.orEmpty()
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()

        val regions = _countryName?.let { db.regionDao().getAll(it) } ?: emptyArray()
        for (region in regions) {
            val regionName = region.name
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@RegionActivity, SectorActivity::class.java)
                intent.putExtra(IntentConstants.REGION_KEY, region.id)
                startActivity(intent)
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    regionName!!,
                    "",
                    WidgetUtils.tableFontSizeDp,
                    onClickListener,
                    Color.WHITE,
                    Typeface.BOLD))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    override fun deleteContent() {
        _countryName?.let {
            db.deleteRegions(it)
        }
    }
}
