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
import android.app.Dialog
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
import com.yacgroup.yacguide.utils.WidgetUtils

import org.json.JSONException

import java.io.*
import java.util.Arrays

class TourbookActivity : BaseNavigationActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _availableYears: IntArray
    private var _currentYearIdx: Int = 0
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
        _initYears()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            Toast.makeText(this, R.string.ascend_deleted, Toast.LENGTH_SHORT).show()
        } else if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IntentConstants.OPEN_TOURBOOK -> data?.data?.also { uri -> _import(uri) }
                IntentConstants.SAVE_TOURBOOK -> data?.data?.also { uri -> _export(uri) }
            }
        }
    }

    fun goToNext(v: View) {
        if (++_currentYearIdx < _availableYears.size) {
            findViewById<View>(R.id.prevButton).visibility = View.VISIBLE
            findViewById<View>(R.id.nextButton).visibility = if (_currentYearIdx == _availableYears.size - 1) View.INVISIBLE else View.VISIBLE
            _displayContent(_availableYears[_currentYearIdx])
        }
    }

    fun goToPrevious(v: View) {
        if (--_currentYearIdx >= 0) {
            findViewById<View>(R.id.nextButton).visibility = View.VISIBLE
            findViewById<View>(R.id.prevButton).visibility = if (_currentYearIdx == 0) View.INVISIBLE else View.VISIBLE
            _displayContent(_availableYears[_currentYearIdx])
        }
    }

    @Throws(IOException::class)
    private fun _readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun _writeTextToUri(uri: Uri, text: String) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(text.toByteArray())
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun _import(uri: Uri) {
        val confirmDialog = Dialog(this)
        confirmDialog.setContentView(R.layout.dialog)
        confirmDialog.findViewById<TextView>(R.id.dialogText).text = getString(
                R.string.override_tourbook)
        confirmDialog.findViewById<View>(R.id.yesButton).setOnClickListener {
            try {
                val jsonString = _readTextFromUri(uri)
                TourbookExporter(_db).importTourbook(jsonString)
                Toast.makeText(this, getString(R.string.tourbook_import_successfull),
                        Toast.LENGTH_SHORT).show()
            } catch (e: JSONException) {
                Toast.makeText(this, getString(R.string.tourbook_import_error),
                        Toast.LENGTH_SHORT).show()
            }
            _initYears()
            confirmDialog.dismiss()
        }
        confirmDialog.findViewById<View>(R.id.noButton).setOnClickListener {
            confirmDialog.dismiss()
        }
        confirmDialog.setCanceledOnTouchOutside(false)
        confirmDialog.setCancelable(false)
        confirmDialog.show()
    }

    private fun _export(uri: Uri) {
        try {
            val jsonString = TourbookExporter(_db).exportTourbook()
            _writeTextToUri(uri, jsonString)
            Toast.makeText(this, getString(R.string.tourbook_export_successfull),
                    Toast.LENGTH_SHORT).show()
        } catch (e: JSONException) {
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

    private fun _displayContent(year: Int) {
        val ascends = _getAscends(year).toMutableList()
        if (!_customSettings.getBoolean(getString(R.string.order_tourbook_chronologically), resources.getBoolean(R.bool.order_tourbook_chronologically))) {
            ascends.reverse()
        }

        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        (findViewById<View>(R.id.currentTextView) as TextView).text = if (year == 0) "" else year.toString()

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
                        "$day.$month.$year",
                        region.name.orEmpty(),
                        WidgetUtils.infoFontSizeDp,
                        View.OnClickListener { },
                        WidgetUtils.tourHeaderColor,
                        Typeface.BOLD,
                        5, 10, 5, 0))
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
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    "${rock.name.orEmpty()} - ${route.name.orEmpty()}",
                    route.grade.orEmpty(),
                    WidgetUtils.tableFontSizeDp,
                    onClickListener,
                    bgColor,
                    Typeface.NORMAL))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    private fun _getAscends(year: Int): List<Ascend> {
        return when (_tourbookType) {
            TourbookType.eAscends -> _db.getAscendsBelowStyleId(year, AscendStyle.eBOTCHED.id)
            TourbookType.eBotches -> _db.getAscendsOfStyle(year, AscendStyle.eBOTCHED.id)
            else -> _db.getAscendsOfStyle(year, AscendStyle.ePROJECT.id)
        }
    }

    private fun _selectFileImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, IntentConstants.OPEN_TOURBOOK)
    }

    private fun _selectFileExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, IntentConstants.SAVE_TOURBOOK)
    }

    private fun _initYears() {
        _availableYears = when (_tourbookType) {
            TourbookType.eAscends -> _db.getYearsBelowStyleId(AscendStyle.eBOTCHED.id)
            TourbookType.eBotches -> _db.getYearsOfStyle(AscendStyle.eBOTCHED.id)
            else -> _db.getYearsOfStyle(AscendStyle.ePROJECT.id)
        }

        Arrays.sort(_availableYears)
        _currentYearIdx = _availableYears.size - 1
        if (_currentYearIdx >= 0) {
            findViewById<View>(R.id.nextButton).visibility = View.INVISIBLE
            findViewById<View>(R.id.prevButton).visibility = if (_availableYears.size > 1) View.VISIBLE else View.INVISIBLE
            _displayContent(_availableYears[_currentYearIdx])
        } else {
            findViewById<View>(R.id.nextButton).visibility = View.INVISIBLE
            findViewById<View>(R.id.prevButton).visibility = View.INVISIBLE
            _displayContent(0)
        }
    }
}
