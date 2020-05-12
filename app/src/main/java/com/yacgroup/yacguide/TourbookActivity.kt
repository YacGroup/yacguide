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

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.yacgroup.yacguide.database.*

import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.FileChooser
import com.yacgroup.yacguide.utils.FilesystemUtils
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

import org.json.JSONException
import java.io.File

import java.util.Arrays

class TourbookActivity : BaseNavigationActivity() {

    private lateinit var _db: AppDatabase
    private lateinit var _availableYears: IntArray
    private var _currentYearIdx: Int = 0
    private var _tourbookType: TourbookType = TourbookType.eAscends
    private var _ioOption: IOOption? = null

    private lateinit var _customSettings: SharedPreferences

    override fun getLayoutId(): Int {
        return R.layout.activity_tourbook
    }

    private enum class IOOption {
        eExport,
        eImport
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
            R.id.action_import -> _import()
            R.id.action_export -> _export()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.ascends)

        _db = AppDatabase.getAppDatabase(this)
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
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (FilesystemUtils.permissionGranted(grantResults)) {
            _showFileChooser()
        } else {
            val errorTextId = if (_ioOption == IOOption.eImport)
                    R.string.import_impossible
                else
                    R.string.export_impossible
            Toast.makeText(this, errorTextId, Toast.LENGTH_SHORT).show()
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

    private fun _import() {
        _ioOption = IOOption.eImport
        if (FilesystemUtils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            _showFileChooser()
        }
    }

    private fun _export() {
        _ioOption = IOOption.eExport
        if (FilesystemUtils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            _showFileChooser()
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
        val ascends = getAscends(year)
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

            var route = _db.routeDao().getRoute(ascend.routeId)
            val rock: Rock
            val sector: Sector
            val region: Region
            if (route == null) {
                // The database entry has been deleted
                route = _db.createUnknownRoute()
                rock = _db.createUnknownRock()
                region = _db.createUnknownRegion()
            } else {
                rock = _db.rockDao().getRock(route.parentId)!!
                sector = _db.sectorDao().getSector(rock.parentId)!!
                region = _db.regionDao().getRegion(sector.parentId)!!
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

    private fun getAscends(year: Int): Array<Ascend> {
        val ascends = when (_tourbookType) {
            TourbookType.eAscends -> _db.ascendDao().getAllBelowStyleId(year, AscendStyle.eBOTCHED.id)
            TourbookType.eBotches -> _db.ascendDao().getAll(year, AscendStyle.eBOTCHED.id)
            else -> _db.ascendDao().getAll(year, AscendStyle.ePROJECT.id)
        }
        return ascends
    }

    private fun _showFileChooser() {
        val defaultFileName = if (_ioOption == IOOption.eExport)
            getString(R.string.tourbook_filename)
        else
            ""
        FileChooser(this, defaultFileName).setFileListener(object : FileChooser.FileSelectedListener {
            override fun fileSelected(file: File) {
                val filePath = file.absolutePath
                if (_ioOption == IOOption.eImport && !file.exists()) {
                    Toast.makeText(this@TourbookActivity, R.string.file_not_existing, Toast.LENGTH_SHORT).show()
                    return
                }
                _showConfirmDialog(filePath)
            }
        }).showDialog()
    }

    private fun _showConfirmDialog(filePath: String) {
        val confirmDialog = Dialog(this)
        confirmDialog.setContentView(R.layout.dialog)
        val infoText = getString(if (_ioOption == IOOption.eExport)
            R.string.override_file
        else
            R.string.override_tourbook)
        confirmDialog.findViewById<TextView>(R.id.dialogText).text = infoText
        confirmDialog.findViewById<View>(R.id.yesButton).setOnClickListener {
            try {
                val exporter = TourbookExporter(_db)
                var successMsg = filePath
                successMsg += if (_ioOption == IOOption.eExport) {
                    exporter.exportTourbook(filePath)
                    getString(R.string.successfully_exported)
                } else {
                    exporter.importTourbook(filePath)
                    getString(R.string.successfully_imported)
                }
                Toast.makeText(this@TourbookActivity, successMsg, Toast.LENGTH_SHORT).show()
            } catch (e: JSONException) {
                Toast.makeText(this@TourbookActivity, R.string.import_export_error, Toast.LENGTH_SHORT).show()
            }

            confirmDialog.dismiss()
            _initYears()
        }
        confirmDialog.findViewById<View>(R.id.noButton).setOnClickListener { confirmDialog.dismiss() }
        confirmDialog.setCanceledOnTouchOutside(false)
        confirmDialog.setCancelable(false)
        confirmDialog.show()
    }

    private fun _initYears() {
        _availableYears = when (_tourbookType) {
            TourbookType.eAscends -> _db.ascendDao().getYearsBelowStyleId(AscendStyle.eBOTCHED.id)
            TourbookType.eBotches -> _db.ascendDao().getYears(AscendStyle.eBOTCHED.id)
            else -> _db.ascendDao().getYears(AscendStyle.ePROJECT.id)
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
