package com.yacgroup.yacguide

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.yacgroup.yacguide.database.AppDatabase
import com.yacgroup.yacguide.database.Ascend
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Sector
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

import java.util.ArrayList

class TourbookAscendActivity : BaseNavigationActivity() {

    private lateinit var _db: AppDatabase
    private var _ascends: Array<Ascend> = emptyArray()
    private var _currentAscendIdx: Int = 0
    private var _maxAscendIdx: Int = 0
    private var _routeId: Int = 0
    private var _resultUpdated: Int = IntentConstants.RESULT_NO_UPDATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = "Tourenbuch"

        _db = AppDatabase.getAppDatabase(this)

        val ascendId = intent.getIntExtra(IntentConstants.ASCEND_KEY, AppDatabase.INVALID_ID)
        _routeId = intent.getIntExtra(IntentConstants.ROUTE_KEY, AppDatabase.INVALID_ID)
        if (ascendId != AppDatabase.INVALID_ID) {
            _db.ascendDao().getAscend(ascendId)?.let {
                _ascends = arrayOf(it)
            }
            _routeId = _ascends[0].routeId
        } else if (_routeId != AppDatabase.INVALID_ID) {
            _ascends = _db.ascendDao().getAscendsForRoute(_routeId)
            findViewById<View>(R.id.nextButton).visibility = if (_ascends.size > 1) View.VISIBLE else View.INVISIBLE
        }
        _maxAscendIdx = _ascends.size - 1
        if (_ascends.isNotEmpty()) {
            _displayContent(_ascends[_currentAscendIdx])
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_tourbook_ascend
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_delete, menu)
        return true
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _resultUpdated = IntentConstants.RESULT_UPDATED
        }
        _ascends[_currentAscendIdx] = _db.ascendDao().getAscend(_ascends[_currentAscendIdx].id)!!
        _displayContent(_ascends[_currentAscendIdx])
    }

    fun goToNext(v: View) {
        if (++_currentAscendIdx <= _maxAscendIdx) {
            findViewById<View>(R.id.prevButton).visibility = View.VISIBLE
            findViewById<View>(R.id.nextButton).visibility = if (_currentAscendIdx == _maxAscendIdx) View.INVISIBLE else View.VISIBLE
            _displayContent(_ascends[_currentAscendIdx])
        }
    }

    fun goToPrevious(v: View) {
        if (--_currentAscendIdx >= 0) {
            findViewById<View>(R.id.nextButton).visibility = View.VISIBLE
            findViewById<View>(R.id.prevButton).visibility = if (_currentAscendIdx == 0) View.INVISIBLE else View.VISIBLE
            _displayContent(_ascends[_currentAscendIdx])
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> edit()
            R.id.action_delete -> delete()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun edit() {
        val intent = Intent(this@TourbookAscendActivity, AscendActivity::class.java)
        intent.putExtra(IntentConstants.ASCEND_KEY, _ascends[_currentAscendIdx].id)
        startActivityForResult(intent, 0)
    }

    private fun delete() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog)
        (dialog.findViewById<View>(R.id.dialogText) as TextView).text = "Diese Begehung löschen?"
        dialog.findViewById<View>(R.id.yesButton).setOnClickListener {
            _db.deleteAscend(_ascends[_currentAscendIdx])
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

        var route = _db.routeDao().getRoute(_routeId)
        val rock: Rock
        val sector: Sector
        val region: Region
        if (route != null) {
            rock = _db.rockDao().getRock(route.parentId) ?: _db.createUnknownRock()
            sector = _db.sectorDao().getSector(rock.parentId) ?: _db.createUnknownSector()
            region = _db.regionDao().getRegion(sector.parentId) ?: _db.createUnknownRegion()
        } else {
            route = _db.createUnknownRoute()
            rock = _db.createUnknownRock()
            sector = _db.createUnknownSector()
            region = _db.createUnknownRegion()
            Toast.makeText(this, "Zugehöriger Weg nicht gefunden.\n" + "Datenbankeintrag wurde scheinbar gelöscht.", Toast.LENGTH_LONG).show()
        }

        val partnerIds = ascend.partnerIds
        val partners = ArrayList<String>()
        for (id in partnerIds.orEmpty()) {
            val partner = _db.partnerDao().getPartner(id)
            partners.add(partner?.name ?: AppDatabase.UNKNOWN_NAME)
        }
        val partnersString = TextUtils.join(", ", partners)

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "${ascend.day}.${ascend.month}.${ascend.year}",
                region.name.orEmpty(),
                WidgetUtils.infoFontSizeDp,
                View.OnClickListener { },
                WidgetUtils.tourHeaderColor,
                Typeface.BOLD,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Teilgebiet",
                "",
                WidgetUtils.textFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                sector.name.orEmpty(),
                "",
                WidgetUtils.tableFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Felsen",
                "",
                WidgetUtils.textFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                rock.name.orEmpty(),
                "",
                WidgetUtils.tableFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Weg",
                "",
                WidgetUtils.textFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "${route.name}   ${route.grade}",
                "",
                WidgetUtils.tableFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Stil",
                "",
                WidgetUtils.textFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                AscendStyle.fromId(ascend.styleId)?.styleName.orEmpty(),
                "",
                WidgetUtils.tableFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Seilpartner",
                "",
                WidgetUtils.textFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                partnersString.takeUnless { it.isEmpty() } ?: " - ",
                "",
                WidgetUtils.tableFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                "Bemerkungen",
                "",
                WidgetUtils.textFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 0, 0))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                ascend.notes?.takeUnless { it.isBlank() } ?: " - ",
                "",
                WidgetUtils.tableFontSizeDp,
                View.OnClickListener { },
                Color.WHITE,
                Typeface.NORMAL,
                10, 10, 10, 10))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
    }

}
