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

        jsonParser = SectorParser(db, this, regionId)
        _region = db.regionDao().getRegion(regionId)

        displayContent()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_table
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

            val rocks = db.rockDao().getAll(sector.id)
            val totalCount = rocks.size
            val ascendedCount = rocks.count { it.ascended }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    sectorName.orEmpty(),
                    "$ascendedCount / $totalCount",
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
