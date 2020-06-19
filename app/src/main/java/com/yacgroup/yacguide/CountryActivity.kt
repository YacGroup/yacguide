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
import android.graphics.Color
import android.graphics.Typeface
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
    }

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
                    country.name,
                    "",
                    WidgetUtils.tableFontSizeDp,
                    onClickListener,
                    Color.WHITE,
                    Typeface.BOLD))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
        if (countries.isEmpty()) {
            displayDownloadButton()
        }
    }

    override fun deleteContent() {
        db.deleteCountriesRecursively()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_country
    }
}
