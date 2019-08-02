package com.example.paetz.yacguide

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout

import com.example.paetz.yacguide.network.CountryParser
import com.example.paetz.yacguide.utils.IntentConstants
import com.example.paetz.yacguide.utils.WidgetUtils

class CountryActivity : TableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        jsonParser = CountryParser(db, this)

        displayContent()
    }

    override fun displayContent() {
        this.title = "YACguide"
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        for (country in db.countryDao().all) {
            val countryName = country.name
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@CountryActivity, RegionActivity::class.java)
                intent.putExtra(IntentConstants.COUNTRY_KEY, countryName)
                startActivity(intent)
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    countryName,
                    "",
                    WidgetUtils.tableFontSizeDp,
                    onClickListener,
                    Color.WHITE,
                    Typeface.BOLD))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    override fun deleteContent() {
        db.deleteCountries()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_table
    }
}
