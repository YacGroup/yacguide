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
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import com.yacgroup.yacguide.database.*
import com.yacgroup.yacguide.database.tourbook.TourbookExportFormat
import com.yacgroup.yacguide.database.tourbook.TourbookExporter
import com.yacgroup.yacguide.utils.*

import org.json.JSONException

import java.io.IOException

import java.util.Arrays

class TourbookActivity : BaseNavigationActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _availableYears: IntArray
    private lateinit var _tourbookExporter: TourbookExporter
    private var _currentYear: Int = 0
    private var _tourbookType: TourbookType = TourbookType.eAscends
    private val _exportResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.also { uri -> _export(uri) }
        }
    }
    private val _importResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.also { uri -> _import(uri) }
        }
    }

    private lateinit var _customSettings: SharedPreferences

    private enum class TourbookType {
        eAscends,
        eBotches
    }

    private val _MIME_TYPE_JSON = "application/json"
    private val _MIME_TYPE_CSV = "text/comma-separated-values"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.my_ascends)

        _db = DatabaseWrapper(this)
        _customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)

        _tourbookExporter = TourbookExporter(_db, contentResolver)

        _currentYear = _initYears()
        _displayContent()
    }

    override fun getLayoutId() = R.layout.activity_tourbook

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.import_export, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_import -> _selectFileImport()
            R.id.action_export -> _selectExportFormat()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        val mostRecentYear = _initYears()
        if (!_availableYears.contains(_currentYear)) {
            // This happens if the current year's only ascend has been removed
            _currentYear = mostRecentYear
        }
        _displayContent()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            Toast.makeText(this, R.string.ascend_deleted, Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun chooseYear(v: View) {
        val dialog = DialogWidgetBuilder(this, R.string.select_year).apply {
            setView(R.layout.numberpicker)
        }.create()
        // we need to call show() before findViewById can be used.
        dialog.show()
        dialog.findViewById<NumberPicker>(R.id.yearPicker)?.apply {
            minValue = 0
            maxValue = _availableYears.size - 1
            displayedValues = _availableYears.map { if (it == 0) getString(R.string.undefined) else it.toString() }.toTypedArray()
            value = _availableYears.indexOf(_currentYear)
            setOnClickListener {
                _currentYear = _availableYears[value]
                _displayContent()
                dialog.dismiss()
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun goToNext(v: View) {
        _currentYear = _availableYears[_availableYears.indexOf(_currentYear) + 1]
        _displayContent()
    }

    @Suppress("UNUSED_PARAMETER")
    fun goToPrevious(v: View) {
        _currentYear = _availableYears[_availableYears.indexOf(_currentYear) - 1]
        _displayContent()
    }

    private fun _import(uri: Uri) {
        DialogWidgetBuilder(this, R.string.warning).apply {
            setMessage(getString(R.string.override_tourbook))
            setIcon(android.R.drawable.ic_dialog_alert)
            setNegativeButton()
            setPositiveButton { _, _ ->
                try {
                    _tourbookExporter.importTourbook(uri)
                    Toast.makeText(
                            this@TourbookActivity,
                            R.string.tourbook_import_successfull,
                            Toast.LENGTH_SHORT
                    ).show()
                } catch (e: JSONException) {
                    // Show only the first part of the detailed error message because
                    // the whole JSON string is contained.
                    _showImportError(e.message.toString(), uri, 200)
                } catch (e: IOException) {
                    // Show the full message because at the moment it is not clear
                    // how the message looks like.
                    _showImportError(e.message.toString(), uri)
                }
                _currentYear = _initYears()
                _displayContent()
            }
        }.show()
    }

    // Show dialog with given message. Optionally, limit the number of characters to show.
    private fun _showImportError(errMsg: String, jsonFile: Uri, maxCharsShow: Int? = null) {
        val contactUtils = ContactUtils(this)
        DialogWidgetBuilder(this, R.string.tourbook_import_error).apply {
            if (maxCharsShow == null) {
                setMessage(errMsg)
            } else {
                setMessage(errMsg.take(maxCharsShow)
                        + if (errMsg.length > maxCharsShow) " ..." else "")
            }
            setIcon(android.R.drawable.ic_dialog_alert)
            setNeutralButton(getString(R.string.report_error)) {_, _ ->
                contactUtils.reportImportError(errMsg, jsonFile)
            }
            setPositiveButton { dialog, _ -> dialog.dismiss() }
        }.show()
    }

    private fun _export(uri: Uri) {
        try {
            _tourbookExporter.exportTourbook(uri)
            Toast.makeText(this, getString(R.string.tourbook_export_successfull),
                    Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, getString(R.string.tourbook_export_error),
                    Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun showAscends(v: View) {
        setTitle(R.string.my_ascends)
        _tourbookType = TourbookType.eAscends
        _currentYear = _initYears()
        _displayContent()
    }

    @Suppress("UNUSED_PARAMETER")
    fun showBotches(v: View) {
        setTitle(R.string.my_botches_with_symbol)
        _tourbookType = TourbookType.eBotches
        _currentYear = _initYears()
        _displayContent()
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
        (findViewById<View>(R.id.currentYearTextView) as TextView).text = if (_currentYear == 0) "" else _currentYear.toString()

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
                        textLeft = if (_currentYear == 0) "" else "$day.$month.$_currentYear",
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
                intent.putExtra(IntentConstants.ASCEND_ID, ascend.id)
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
        }
    }

    private fun _selectFileImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = _MIME_TYPE_JSON
        }
        _importResultLauncher.launch(intent)
    }

    private fun _selectExportFormat() {
        var mimeType = _MIME_TYPE_JSON
        DialogWidgetBuilder(this, R.string.export_format).apply {
            setSingleChoiceItems(R.array.exportFormats, _tourbookExporter.exportFormat.id) {_, which ->
                _tourbookExporter.exportFormat = TourbookExportFormat.fromId(which)!!
                if (which == TourbookExportFormat.eCSV.id) {
                    mimeType = _MIME_TYPE_CSV
                }
            }
            setNegativeButton()
            setPositiveButton{ _, _ -> _selectExportFile(mimeType) }
        }.show()
    }

    private fun _selectExportFile(mimeType: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
        }
        _exportResultLauncher.launch(intent)
    }

    private fun _initYears(): Int {
        _availableYears = when (_tourbookType) {
            TourbookType.eAscends -> _db.getYearsBelowStyleId(AscendStyle.eBOTCHED.id)
            TourbookType.eBotches -> _db.getYearsOfStyle(AscendStyle.eBOTCHED.id)
        }
        Arrays.sort(_availableYears)

        return if (_availableYears.isEmpty()) 0 else _availableYears.last()
    }
}
