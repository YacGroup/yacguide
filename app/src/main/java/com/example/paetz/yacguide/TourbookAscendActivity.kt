package com.example.paetz.yacguide

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.database.Ascend
import com.example.paetz.yacguide.database.Region
import com.example.paetz.yacguide.database.Rock
import com.example.paetz.yacguide.database.Sector
import com.example.paetz.yacguide.utils.AscendStyle
import com.example.paetz.yacguide.utils.IntentConstants
import com.example.paetz.yacguide.utils.WidgetUtils

import java.util.ArrayList

class TourbookAscendActivity : AppCompatActivity() {

    private var _db: AppDatabase? = null
    private var _ascends: Array<Ascend> = emptyArray()
    private var _currentAscendIdx: Int = 0
    private var _maxAscendIdx: Int = 0
    private var _routeId: Int = 0
    private var _resultUpdated: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tourbook_ascend)
        this.title = "Tourenbuch"

        _db = AppDatabase.getAppDatabase(this)
        _resultUpdated = IntentConstants.RESULT_NO_UPDATE
        val ascendId = intent.getIntExtra(IntentConstants.ASCEND_KEY, AppDatabase.INVALID_ID)
        _routeId = intent.getIntExtra(IntentConstants.ROUTE_KEY, AppDatabase.INVALID_ID)
        _currentAscendIdx = 0
        if (ascendId != AppDatabase.INVALID_ID) {
            _ascends = arrayOf(_db!!.ascendDao().getAscend(ascendId))
            _routeId = _ascends[0].routeId
        } else if (_routeId != AppDatabase.INVALID_ID) {
            _ascends = _db!!.ascendDao().getAscendsForRoute(_routeId)
            findViewById<View>(R.id.nextAscendButton).visibility = if (_ascends.size > 1) View.VISIBLE else View.INVISIBLE
        }
        _maxAscendIdx = _ascends.size - 1
        if (_ascends.isNotEmpty()) {
            _displayContent(_ascends[_currentAscendIdx])
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _resultUpdated = IntentConstants.RESULT_UPDATED
        }
        _ascends[_currentAscendIdx] = _db!!.ascendDao().getAscend(_ascends[_currentAscendIdx].id)
        _displayContent(_ascends[_currentAscendIdx])
    }

    fun home(v: View) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    fun back(v: View) {
        val resultIntent = Intent()
        setResult(_resultUpdated, resultIntent)
        finish()
    }

    fun goToNextAscend(v: View) {
        if (++_currentAscendIdx <= _maxAscendIdx) {
            findViewById<View>(R.id.prevAscendButton).visibility = View.VISIBLE
            findViewById<View>(R.id.nextAscendButton).visibility = if (_currentAscendIdx == _maxAscendIdx) View.INVISIBLE else View.VISIBLE
            _displayContent(_ascends[_currentAscendIdx])
        }
    }

    fun goToPreviousAscend(v: View) {
        if (--_currentAscendIdx >= 0) {
            findViewById<View>(R.id.nextAscendButton).visibility = View.VISIBLE
            findViewById<View>(R.id.prevAscendButton).visibility = if (_currentAscendIdx == 0) View.INVISIBLE else View.VISIBLE
            _displayContent(_ascends[_currentAscendIdx])
        }
    }

    fun edit(v: View) {
        val intent = Intent(this@TourbookAscendActivity, AscendActivity::class.java)
        val ascend = _ascends[_currentAscendIdx]
        intent.putExtra(IntentConstants.ASCEND_KEY, ascend.id)
        startActivityForResult(intent, 0)
    }

    fun delete(v: View) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog)
        (dialog.findViewById<View>(R.id.dialogText) as TextView).text = "Diese Begehung löschen?"
        dialog.findViewById<View>(R.id.yesButton).setOnClickListener {
            _db!!.deleteAscend(_ascends[_currentAscendIdx])
            dialog.dismiss()
            val resultIntent = Intent()
            setResult(IntentConstants.RESULT_UPDATED, resultIntent)
            finish()
        }
        dialog.findViewById<View>(R.id.noButton).setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun _displayContent(ascend: Ascend) {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()

        var route = _db!!.routeDao().getRoute(_routeId)
        val rock: Rock?
        val sector: Sector
        val region: Region
        if (route !=
                null) {
            rock = _db!!.rockDao().getRock(route.parentId)
            sector = _db!!.sectorDao().getSector(rock!!.parentId)
            region = _db!!.regionDao().getRegion(sector.parentId)
        } else {
            route = _db!!.createUnknownRoute()
            rock = _db!!.createUnknownRock()
            sector = _db!!.createUnknownSector()
            region = _db!!.createUnknownRegion()
            Toast.makeText(this, "Zugehöriger Weg nicht gefunden.\n" + "Datenbankeintrag wurde scheinbar gelöscht.", Toast.LENGTH_LONG).show()
        }

        val partnerIds = ascend.partnerIds
        val partners = ArrayList<String>()
        for (id in partnerIds!!) {
            val partner = _db!!.partnerDao().getPartner(id)
            partners.add(partner?.name ?: AppDatabase.UNKNOWN_NAME)
        }
        val partnersString = TextUtils.join(", ", partners)

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                ascend.day.toString() + "." + ascend.month + "." + ascend.year,
                region.name,
                WidgetUtils.infoFontSizeDp, null,
                -0x444445,
                Typeface.BOLD,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Teilgebiet",
                "",
                WidgetUtils.textFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                sector.name,
                "",
                WidgetUtils.tableFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Felsen",
                "",
                WidgetUtils.textFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                rock.name,
                "",
                WidgetUtils.tableFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Weg",
                "",
                WidgetUtils.textFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                route.name + "   " + route.grade,
                "",
                WidgetUtils.tableFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Stil",
                "",
                WidgetUtils.textFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                AscendStyle.fromId(ascend.styleId)!!.styleName,
                "",
                WidgetUtils.tableFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Seilpartner",
                "",
                WidgetUtils.textFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                if (partnersString.isEmpty()) " - " else partnersString,
                "",
                WidgetUtils.tableFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Bemerkungen",
                "",
                WidgetUtils.textFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                if (ascend.notes!!.isEmpty()) " - " else ascend.notes,
                "",
                WidgetUtils.tableFontSizeDp, null,
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
    }

}
