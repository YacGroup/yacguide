package com.example.paetz.yacguide

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout

import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.database.Comment.RegionComment
import com.example.paetz.yacguide.database.Region
import com.example.paetz.yacguide.network.SectorParser
import com.example.paetz.yacguide.utils.IntentConstants
import com.example.paetz.yacguide.utils.WidgetUtils

class SectorActivity : TableActivity() {

    private var _region: Region? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val regionId = intent.getIntExtra(IntentConstants.REGION_KEY, AppDatabase.INVALID_ID)
        super.initialize(R.layout.activity_sector)

        jsonParser = SectorParser(db, this, regionId)
        _region = db.regionDao().getRegion(regionId)

        displayContent()
    }

    fun showComments(v: View) {
        val dialog = prepareCommentDialog()

        val layout = dialog.findViewById<View>(R.id.commentLayout) as LinearLayout
        for (comment in db.regionCommentDao().getAll(_region!!.id)) {
            val qualityId = comment.qualityId
            val text = comment.text

            layout.addView(WidgetUtils.createHorizontalLine(this, 5))
            if (RegionComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Bedeutung:",
                        RegionComment.QUALITY_MAP[qualityId],
                        WidgetUtils.textFontSizeDp,
                        null,
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0))
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    text,
                    "",
                    WidgetUtils.textFontSizeDp, null,
                    Color.WHITE,
                    Typeface.NORMAL,
                    10, 10, 10, 10))
        }
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        this.title = _region!!.name
        for (sector in db.sectorDao().getAll(_region!!.id)) {
            val sectorName = sector.name
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@SectorActivity, RockActivity::class.java)
                intent.putExtra(IntentConstants.SECTOR_KEY, sector.id)
                startActivity(intent)
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    sectorName!!,
                    "",
                    WidgetUtils.tableFontSizeDp,
                    onClickListener,
                    Color.WHITE,
                    Typeface.BOLD))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    override fun deleteContent() {
        db.deleteSectors(_region!!.id)
    }
}
