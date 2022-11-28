/*
 * Copyright (C) 2019, 2022 Axel Paetzold
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
import android.widget.LinearLayout
import android.widget.Toast

import com.yacgroup.yacguide.database.*
import com.yacgroup.yacguide.utils.*

class TourbookAscendActivity : BaseNavigationActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _ascend: Ascend

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.menu_tourbook)

        _db = DatabaseWrapper(this)

        val ascendId = intent.getIntExtra(IntentConstants.ASCEND_ID, DatabaseWrapper.INVALID_ID)
        _ascend = _db.getAscend(ascendId)!!
    }

    override fun getLayoutId() = R.layout.activity_tourbook_ascend

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_delete, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        _ascend = _db.getAscend(_ascend.id)!!
        _displayContent()
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
        startActivity(Intent(this@TourbookAscendActivity, AscendActivity::class.java).apply {
            putExtra(IntentConstants.ASCEND_ID, _ascend.id)
        })
    }

    private fun delete() {
        DialogWidgetBuilder(this, R.string.dialog_question_delete_ascend).apply {
            setIcon(android.R.drawable.ic_dialog_alert)
            setNegativeButton()
            setPositiveButton { _, _ ->
                _db.deleteAscend(_ascend)
                Toast.makeText(this@TourbookAscendActivity, R.string.ascend_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
        }.show()
    }

    private fun _displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()

        var route = _db.getRoute(_ascend.routeId)
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

        val partnerNames = _db.getPartnerNames(_ascend.partnerIds?.toList().orEmpty())
        val partnersString = TextUtils.join(", ", partnerNames)

        layout.addView(WidgetUtils.createCommonRowLayout(this,
                textLeft = "${_ascend.day}.${_ascend.month}.${_ascend.year}",
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
                textRight = AscendStyle.fromId(_ascend.styleId)?.styleName.orEmpty()))
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
                textLeft = _ascend.notes?.takeUnless { it.isBlank() } ?: " - "))
        layout.addView(WidgetUtils.createHorizontalLine(this, 1))
    }

}
