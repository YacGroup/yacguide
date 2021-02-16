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

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.yacgroup.yacguide.database.*
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.WidgetUtils

import org.json.JSONException

import java.io.IOException

import java.util.Arrays

class TourbookActivity : BaseNavigationActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _availableYears: IntArray
    private var _currentYear: Int = 0
    private var _tourbookType: TourbookType = TourbookType.eAscends

    private lateinit var _customSettings: SharedPreferences

    override fun getLayoutId(): Int {
        return R.layout.activity_tourbook
    }

    private enum class TourbookType {
        eAscends,
        eBotches,
        eProjects
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.import_export, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_import -> _selectFileImport()
            R.id.action_export -> _selectFileExport()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.ascends)

        _db = DatabaseWrapper(this)
        _customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)

        _initYears()
    }

    override fun onResume() {
        super.onResume()
        _initYears(resetCurrentYear = false)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            Toast.makeText(this, R.string.ascend_deleted, Toast.LENGTH_SHORT).show()
        } else if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IntentConstants.REQUEST_OPEN_TOURBOOK -> data?.data?.also { uri -> _import(uri) }
                IntentConstants.REQUEST_SAVE_TOURBOOK -> data?.data?.also { uri -> _export(uri) }
            }
        }
    }

    fun goToNext(v: View) {
        _currentYear = _availableYears[_availableYears.indexOf(_currentYear) + 1]
        _displayContent()
    }

    fun goToPrevious(v: View) {
        _currentYear = _availableYears[_availableYears.indexOf(_currentYear) - 1]
        _displayContent()
    }

    private fun _import(uri: Uri) {
        val confirmDialog = AlertDialog.Builder(this).apply {
            setTitle(R.string.warning)
            setMessage(getString(R.string.override_tourbook))
            setIcon(android.R.drawable.ic_dialog_alert)
            setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
            setPositiveButton(R.string.ok) { dialog, _ ->
                try {
                    TourbookExporter(_db, contentResolver).importTourbook(uri)
                    _makeToast(getString(R.string.tourbook_import_successfull))
                } catch (e: JSONException) {
                    // Show only the first part of the detailed error message because
                    // the whole JSON string is contained.
                    _showImportError(e.message.toString(), 200)
                } catch (e: IOException) {
                    // Show the full message because at the moment it is not clear
                    // how the message looks like.
                    _showImportError(e.message.toString())
                }
                _initYears()
            }
        }
        confirmDialog.show()
    }

    // Show dialog with given message. Optionally, limit the number of characters to show.
    private fun _showImportError(errMsg: String, maxCharsShow: Int? = null) {
        val dialog = AlertDialog.Builder(this).apply {
            setTitle(R.string.tourbook_import_error)
            if (maxCharsShow == null) {
                setMessage(errMsg)
            } else {
                setMessage(errMsg.take(maxCharsShow)
                        + if (errMsg.length > maxCharsShow) " ..." else "")
            }
            setIcon(android.R.drawable.ic_dialog_alert)
            setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        }
        dialog.show()
    }

    private fun _makeToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun _export(uri: Uri) {
        try {
            TourbookExporter(_db, contentResolver).exportTourbook(uri)
            Toast.makeText(this, getString(R.string.tourbook_export_successfull),
                    Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, getString(R.string.tourbook_export_error),
                    Toast.LENGTH_SHORT).show()
        }
    }

    fun showAscends(v: View) {
        setTitle(R.string.ascends)
        _tourbookType = TourbookType.eAscends
        _initYears()
    }

    fun showBotches(v: View) {
        setTitle(R.string.botch_text_symbol)
        _tourbookType = TourbookType.eBotches
        _initYears()
    }

    fun showProjects(v: View) {
        setTitle(R.string.project_text_symbol)
        _tourbookType = TourbookType.eProjects
        _initYears()
    }

    private fun _displayContent() {
        findViewById<View>(R.id.nextButton).visibility = if (_isLastYear()) View.INVISIBLE else View.VISIBLE
        findViewById<View>(R.id.prevButton).visibility = if (_isFirstYear()) View.INVISIBLE else View.VISIBLE

        val ascends = _getAscends().toMutableList()
        if (!_customSettings.getBoolean(getString(R.string.order_tourbook_chronologically), resources.getBoolean(R.bool.order_tourbook_chronologically))) {
            ascends.reverse()
        }

        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        (findViewById<View>(R.id.currentTextView) as TextView).text = if (_currentYear == 0) "" else _currentYear.toString()

        var currentMonth = -1
        var currentDay = -1
        var currentRegionId = -1

        val defaultColor = ContextCompat.getColor(this, R.color.white)
        var leadColor = defaultColor
        var followColor = defaultColor
        if (_customSettings.getBoolean(getString(R.string.colorize_tourbook_entries),
                                       resources.getBoolean(R.bool.colorize_tourbook_entries))) {
            leadColor = _customSettings.getInt(getString(R.string.lead), defaultColor)
            followColor = _customSettings.getInt(getString(R.string.follow), defaultColor)
        }
        for (ascend in ascends) {
            val month = ascend.month
            val day = ascend.day

            var route = _db.getRoute(ascend.routeId)
            val rock: Rock
            val sector: Sector
            val region: Region
            if (route == null) {
                // The database entry has been deleted
                route = _db.createUnknownRoute()
                rock = _db.createUnknownRock()
                region = _db.createUnknownRegion()
            } else {
                rock = _db.getRock(route.parentId)!!
                sector = _db.getSector(rock.parentId)!!
                region = _db.getRegion(sector.parentId)!!
            }

            if (month != currentMonth || day != currentDay || region.id != currentRegionId) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        textLeft = "$day.$month.$_currentYear",
                        textRight = region.name.orEmpty(),
                        textSizeDp = WidgetUtils.infoFontSizeDp,
                        bgColor = WidgetUtils.tourHeaderColor))
                layout.addView(WidgetUtils.createHorizontalLine(this, 5))
                currentMonth = month
                currentDay = day
                currentRegionId = region.id
            }
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@TourbookActivity, TourbookAscendActivity::class.java)
                intent.putExtra(IntentConstants.ASCEND_KEY, ascend.id)
                startActivityForResult(intent, 0)
            }

            val bgColor = when {
                AscendStyle.isLead(AscendStyle.bitMask(ascend.styleId)) -> leadColor
                AscendStyle.isFollow(AscendStyle.bitMask(ascend.styleId)) -> followColor
                else -> defaultColor
            }
            val rockName = ParserUtils.decodeObjectNames(rock.name)
            val routeName = ParserUtils.decodeObjectNames(route.name)
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = "${rockName.first} - ${routeName.first}",
                    textRight = route.grade.orEmpty(),
                    onClickListener = onClickListener,
                    bgColor = bgColor))
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = "${rockName.second} - ${routeName.second}",
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    onClickListener = onClickListener,
                    bgColor = bgColor,
                    typeface = Typeface.NORMAL))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    private fun _isFirstYear(): Boolean {
        return _availableYears.isEmpty() || _currentYear == _availableYears.first()
    }

    private fun _isLastYear(): Boolean {
        return _availableYears.isEmpty() || _currentYear == _availableYears.last()
    }

    private fun _getAscends(): List<Ascend> {
        return when (_tourbookType) {
            TourbookType.eAscends -> _db.getAscendsBelowStyleId(_currentYear, AscendStyle.eBOTCHED.id)
            TourbookType.eBotches -> _db.getAscendsOfStyle(_currentYear, AscendStyle.eBOTCHED.id)
            else -> _db.getAscendsOfStyle(_currentYear, AscendStyle.ePROJECT.id)
        }
    }

    private fun _selectFileImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, IntentConstants.REQUEST_OPEN_TOURBOOK)
    }

    private fun _selectFileExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, IntentConstants.REQUEST_SAVE_TOURBOOK)
    }

    private fun _initYears(resetCurrentYear: Boolean = true) {
        _availableYears = when (_tourbookType) {
            TourbookType.eAscends -> _db.getYearsBelowStyleId(AscendStyle.eBOTCHED.id)
            TourbookType.eBotches -> _db.getYearsOfStyle(AscendStyle.eBOTCHED.id)
            else -> _db.getYearsOfStyle(AscendStyle.ePROJECT.id)
        }

        Arrays.sort(_availableYears)
        if (resetCurrentYear && _availableYears.isNotEmpty()) {
            _currentYear = _availableYears.last()
        }
        _displayContent()
    }
}
