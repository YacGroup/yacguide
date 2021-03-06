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
import com.yacgroup.yacguide.network.CountryParser
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

class CountryActivity : UpdatableTableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        jsonParser = CountryParser(db, this)

        WhatsNewInfo(this).let {
            if (it.checkForVersionUpdate()) {
                it.showDialog()
            }
        }
    }

    override fun getLayoutId() = R.layout.activity_country

    override fun displayContent() {
        setTitle(R.string.app_name)
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        val countries = db.getCountries()
        for (country in countries) {
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@CountryActivity, RegionActivity::class.java)
                intent.putExtra(IntentConstants.COUNTRY_KEY, country.name)
                startActivity(intent)
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = country.name,
                    onClickListener = onClickListener,
                    padding = WidgetUtils.Padding(20, 30, 20, 30)))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
        if (countries.isEmpty()) {
            displayDownloadButton()
        }
    }

    override fun deleteContent() = db.deleteCountriesRecursively()

    override fun searchRocks() = db.getRocks()

    override fun searchProjects() = db.getProjectedRocks()

    override fun searchBotches() = db.getBotchedRocks()
}
