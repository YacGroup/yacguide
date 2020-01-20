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
