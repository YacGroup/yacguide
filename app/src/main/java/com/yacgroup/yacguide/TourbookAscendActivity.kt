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
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast

import com.yacgroup.yacguide.database.*
import com.yacgroup.yacguide.utils.*

class TourbookAscendActivity : BaseNavigationActivity() {

    private lateinit var _db: DatabaseWrapper
    private var _ascends: MutableList<Ascend> = mutableListOf<Ascend>()
    private var _currentAscendIdx: Int = 0
    private var _maxAscendIdx: Int = 0
    private var _routeId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.menu_tourbook)

        _db = DatabaseWrapper(this)

        val ascendId = intent.getIntExtra(IntentConstants.ASCEND_KEY, DatabaseWrapper.INVALID_ID)
        _routeId = intent.getIntExtra(IntentConstants.ROUTE_KEY, DatabaseWrapper.INVALID_ID)
        if (ascendId != DatabaseWrapper.INVALID_ID) {
            _db.getAscend(ascendId)?.let {
                _ascends = mutableListOf(it)
            }
            _routeId = _ascends[0].routeId
        } else if (_routeId != DatabaseWrapper.INVALID_ID) {
            _ascends = _db.getRouteAscends(_routeId).toMutableList()
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
            Toast.makeText(this, getString(R.string.ascends_refreshed), Toast.LENGTH_SHORT).show()
            _ascends[_currentAscendIdx] = _db.getAscend(_ascends[_currentAscendIdx].id)!!
            _displayContent(_ascends[_currentAscendIdx])
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun goToNext(v: View) {
        if (++_currentAscendIdx <= _maxAscendIdx) {
            findViewById<View>(R.id.prevButton).visibility = View.VISIBLE
            findViewById<View>(R.id.nextButton).visibility = if (_currentAscendIdx == _maxAscendIdx) View.INVISIBLE else View.VISIBLE
            _displayContent(_ascends[_currentAscendIdx])
        }
    }

    @Suppress("UNUSED_PARAMETER")
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
        val builder = DialogWidgetBuilder(this, R.string.dialog_question_delete_ascend).apply {
            setIcon(android.R.drawable.ic_dialog_alert)
            setNegativeButton()
            setPositiveButton { _, _ ->
                _db.deleteAscend(_ascends[_currentAscendIdx])
                val resultIntent = Intent()
                setResult(IntentConstants.RESULT_UPDATED, resultIntent)
                finish()
            }
        }
        builder.show()
    }

    private fun _displayContent(ascend: Ascend) {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()

        var route = _db.getRoute(_routeId)
        val rock: Rock
        val sector: Sector
        val region: Region
        if (route != null) {
            rock = _db.getRock(route.parentId) ?: _db.createUnknownRock()
            sector = _db.getSector(rock.parentId) ?: _db.createUnknownSector()
            region = _db.getRegion(sector.parentId) ?: _db.createUnknownRegion()
        } else {
            route = _db.createUnknownRoute()
            rock = _db.createUnknownRock()
            sector = _db.createUnknownSector()
            region = _db.createUnknownRegion()
            Toast.makeText(this, R.string.corresponding_route_not_found, Toast.LENGTH_LONG).show()
        }

        val partnerNames = _db.getPartnerNames(ascend.partnerIds?.toList().orEmpty())
        val partnersString = TextUtils.join(", ", partnerNames)

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = "${ascend.day}.${ascend.month}.${ascend.year}",
                textRight = region.name.orEmpty(),
                textSizeDp = WidgetUtils.infoFontSizeDp,
                bgColor = WidgetUtils.tourHeaderColor))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = getString(R.string.region),
                textSizeDp = WidgetUtils.textFontSizeDp,
                typeface = Typeface.NORMAL))
        val sectorName = ParserUtils.decodeObjectNames(sector.name)
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = sectorName.first,
                textRight = sectorName.second))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = getString(R.string.rock),
                textSizeDp = WidgetUtils.textFontSizeDp,
                typeface = Typeface.NORMAL))
        val rockName = ParserUtils.decodeObjectNames(rock.name)
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = rockName.first,
                textRight = rockName.second))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = getString(R.string.route),
                textSizeDp = WidgetUtils.textFontSizeDp,
                typeface = Typeface.NORMAL))
        val routeName = ParserUtils.decodeObjectNames(route.name)
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = routeName.first,
                textRight = routeName.second))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = getString(R.string.grade),
                textRight = getString(R.string.style),
                textSizeDp = WidgetUtils.textFontSizeDp,
                typeface = Typeface.NORMAL))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = route.grade.orEmpty(),
                textRight = AscendStyle.fromId(ascend.styleId)?.styleName.orEmpty()))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = getString(R.string.partner),
                textSizeDp = WidgetUtils.textFontSizeDp,
                typeface = Typeface.NORMAL))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = partnersString.takeUnless { it.isEmpty() } ?: " - "))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = getString(R.string.notes),
                textSizeDp = WidgetUtils.textFontSizeDp,
                typeface = Typeface.NORMAL))
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = ascend.notes?.takeUnless { it.isBlank() } ?: " - "))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
    }

}
