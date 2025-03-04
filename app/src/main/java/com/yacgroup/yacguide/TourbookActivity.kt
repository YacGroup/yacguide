/*
 * Copyright (C) 2019, 2022, 2023 Axel Paetzold
 * Copyright (C) 2023 Christian Sommer
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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.yacgroup.yacguide.database.*
import com.yacgroup.yacguide.database.tourbook.*
import com.yacgroup.yacguide.databinding.ActivityTourbookBinding
import com.yacgroup.yacguide.databinding.NumberpickerBinding
import com.yacgroup.yacguide.list_adapters.*
import com.yacgroup.yacguide.utils.*
import org.json.JSONException
import java.io.IOException
import java.util.*

class TourbookActivity : BaseNavigationActivity<ActivityTourbookBinding>() {

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
    private lateinit var _visualUtils: VisualUtils
    private lateinit var _viewAdapter: SectionViewAdapter<Ascend>
    private lateinit var _db: DatabaseWrapper
    private lateinit var _customSettings: SharedPreferences
    private lateinit var _availableYears: IntArray
    private var _tourbookExporter: BaseExporter? = null
    private var _currentYear: Int = -1
    private var _tourbookType: TourbookType = TourbookType.eAscends

    private enum class TourbookType {
        eAscends,
        eBotches
    }

    override fun getViewBinding() = ActivityTourbookBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.my_ascends)

        _db = DatabaseWrapper(this)
        _customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)
        _visualUtils = VisualUtils(
            context = this,
            customSettings = _customSettings,
            colorizeLeadsAndFollows = _customSettings.getBoolean(
                getString(R.string.pref_key_colorize_tourbook_entries),
                resources.getBoolean(R.bool.pref_default_colorize_tourbook_entries)))
        _viewAdapter = SectionViewAdapter(_visualUtils) {
            ListViewAdapter(ItemDiffCallback(
                _areItemsTheSame = { ascend1, ascend2 -> ascend1.id == ascend2.id },
                _areContentsTheSame = { ascend1, ascend2 -> ascend1 == ascend2 }
            )) { ascend ->
                val route = _db.getRoute(ascend.routeId) ?: _db.createUnknownRoute()
                val rock = _db.getRock(route.parentId) ?: _db.createUnknownRock()
                val routeName = ParserUtils.decodeObjectNames(route.name)
                val rockName = ParserUtils.decodeObjectNames(rock.name)
                ListItem(
                    backgroundColor = _getAscendBackground(ascend.styleId),
                    mainText = Pair("${rockName.first} - ${routeName.first}", route.grade.orEmpty()),
                    subText = "${rockName.second} - ${routeName.second}",
                    onClick = { _onAscendSelected(ascend) })
            }
        }
        activityViewBinding.layoutListViewContent.tableRecyclerView.adapter = _viewAdapter
    }

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

    @Suppress("UNUSED_PARAMETER")
    fun chooseYear(v: View) {
        DialogWidgetBuilder(this).create().also { dialog ->
            NumberpickerBinding.inflate(dialog.layoutInflater).also { binding ->
                dialog.setView(binding.root)
                binding.yearPicker.apply {
                    minValue = 0
                    maxValue = _availableYears.size - 1
                    displayedValues =
                        _availableYears.map { if (it == 0) getString(R.string.undefined) else it.toString() }
                            .toTypedArray()
                    value = _availableYears.indexOf(_currentYear)
                    setOnClickListener {
                        _currentYear = _availableYears[value]
                        _displayContent()
                        dialog.dismiss()
                    }
                }
            }
        }.show()
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
                    JsonImporter(_db, contentResolver).import(uri)
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
            _tourbookExporter!!.export(uri)
            Toast.makeText(this, getString(R.string.tourbook_export_successfull),
                    Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
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
        activityViewBinding.layoutNavTourbook.apply {
            nextButton.visibility = if (_isLastYear()) View.INVISIBLE else View.VISIBLE
            prevButton.visibility = if (_isFirstYear()) View.INVISIBLE else View.VISIBLE
            currentYearTextView.text = if (_currentYear == 0) "" else _currentYear.toString()
        }

        val ascends = _getAscends().toMutableList()
        if (!_customSettings.getBoolean(
                getString(R.string.pref_key_order_tourbook_chronologically),
                resources.getBoolean(R.bool.pref_default_order_tourbook_chronologically))
            ) {
            ascends.reverse()
        }
        val sectionViews = ascends.groupBy {
            Pair(if (_currentYear == 0) "" else "${it.day}.${it.month}.$_currentYear",
                 (_db.getRegionForRoute(it.routeId) ?: _db.createUnknownRegion()).name.orEmpty())
        }.map {
            SectionViewItem(
                title = it.key,
                elements = it.value)
        }

        _viewAdapter.submitList(sectionViews)
    }

    private fun _isFirstYear(): Boolean {
        return _availableYears.isEmpty() || _currentYear == _availableYears.first()
    }

    private fun _isLastYear(): Boolean {
        return _availableYears.isEmpty() || _currentYear == _availableYears.last()
    }

    private fun _getAscends(): List<Ascend> {
        return when (_tourbookType) {
            TourbookType.eAscends -> _db.getAscendsOfYearBelowStyleId(_currentYear, AscendStyle.eBOTCHED.id)
            TourbookType.eBotches -> _db.getAscendsOfYearAndStyle(_currentYear, AscendStyle.eBOTCHED.id)
        }
    }

    private fun _selectFileImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            /*
             * NOTE:
             * The reason is unclear, why the MIME type filter makes the import for other locations
             * than the "Downloads" folder impossible for API version <= 28.
             */
            type = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) "*/*" else TourbookExportFormat.eJSON.mimeType
        }
        _importResultLauncher.launch(intent)
    }

    private fun _selectExportFormat() {
        var format: TourbookExportFormat = TourbookExportFormat.eJSON
        DialogWidgetBuilder(this, R.string.export_format).apply {
            setSingleChoiceItems(R.array.exportFormats, format.id) { _, which ->
                format = TourbookExportFormat.fromId(which)!!
            }
            setNegativeButton()
            setPositiveButton{ _, _ ->
                _tourbookExporter = TourbookExporterFactory(_db, contentResolver).create(format)
                _selectExportFile(format)
            }
        }.show()
    }

    private fun _selectExportFile(format: TourbookExportFormat) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(
                Intent.EXTRA_TITLE,
                "${getString(R.string.default_export_tourbook_name)}.${format.extension}"
            )
            type = format.mimeType
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

    private fun _getAscendBackground(styleId: Int): Int {
        return AscendStyle.deriveAscentColor(
            ascentBitMask = AscendStyle.bitMask(styleId),
            leadColor = _visualUtils.leadBgColor,
            followColor = _visualUtils.followBgColor,
            defaultColor = _visualUtils.defaultBgColor)
    }

    private fun _onAscendSelected(ascend: Ascend) {
        startActivity(Intent(this@TourbookActivity, TourbookAscendActivity::class.java).apply {
            putExtra(IntentConstants.ASCEND_ID, ascend.id)
        })
    }
}
