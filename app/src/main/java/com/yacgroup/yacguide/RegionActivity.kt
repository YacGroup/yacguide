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
import com.yacgroup.yacguide.activity_properties.*
import com.yacgroup.yacguide.network.RegionParser
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

class RegionActivity : TableActivityWithOptionsMenu() {

    private lateinit var _updatable: Updatable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _updatable = Updatable(
            this,
            RegionParser(db, activityLevel.parentName)
        ) { db.deleteRegionsRecursively(activityLevel.parentName) }
        properties = arrayListOf(RockSearchable(this), AscentFilterable(this), _updatable)
    }

    override fun getLayoutId() = R.layout.activity_region

    override fun displayContent() {
        this.title = activityLevel.parentName
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        val regions = db.getRegions(activityLevel.parentName)
        for (region in regions) {
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@RegionActivity, SectorActivity::class.java)
                intent.putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eSector.value)
                intent.putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, region.id)
                intent.putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, region.name)
                startActivity(intent)
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = region.name.orEmpty(),
                    onClickListener = onClickListener,
                    padding = WidgetUtils.Padding(20, 30, 20, 30)))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
        if (regions.isEmpty()) {
            layout.addView(_updatable.getDownloadButton())
        }
    }
}
