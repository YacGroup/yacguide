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
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.yacgroup.yacguide.database.Rock

import com.yacgroup.yacguide.network.RegionParser
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

class RegionActivity : UpdatableTableActivity() {

    private lateinit var _countryName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _countryName = intent.getStringExtra(IntentConstants.COUNTRY_KEY)
        jsonParser = RegionParser(db, this, _countryName)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_region
    }

    override fun displayContent() {
        this.title = _countryName.orEmpty()
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        val regions = db.getRegions(_countryName)
        for (region in regions) {
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@RegionActivity, SectorActivity::class.java)
                intent.putExtra(IntentConstants.REGION_KEY, region.id)
                startActivity(intent)
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = region.name.orEmpty(),
                    onClickListener = onClickListener,
                    padding = WidgetUtils.Padding(20, 30, 20, 30)))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
        if (regions.isEmpty()) {
            displayDownloadButton()
        }
    }

    override fun deleteContent() {
        db.deleteRegionsRecursively(_countryName)
    }

    override fun searchRocks(rockName: String): List<Rock> {
        return db.getRocksForCountry(_countryName).filter { rock -> rock.name!!.toLowerCase().contains(rockName.toLowerCase()) }
    }
}
