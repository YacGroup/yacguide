package com.example.paetz.yacguide

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast

import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.database.Region
import com.example.paetz.yacguide.database.Rock
import com.example.paetz.yacguide.database.Sector
import com.example.paetz.yacguide.database.TourbookExporter
import com.example.paetz.yacguide.utils.AscendStyle
import com.example.paetz.yacguide.utils.FileChooser
import com.example.paetz.yacguide.utils.FilesystemUtils
import com.example.paetz.yacguide.utils.IntentConstants
import com.example.paetz.yacguide.utils.WidgetUtils

import org.json.JSONException

import java.io.File
import java.util.Arrays

class TourbookActivity : AppCompatActivity() {

    private val _FILE_NAME = "Tourenbuch.json"

    private var _db: AppDatabase? = null
    private var _availableYears: IntArray? = null
    private var _currentYearIdx: Int = 0
    private var _maxYearIdx: Int = 0
    private var _tourbookType: TourbookType? = null
    private var _exporter: TourbookExporter? = null
    private var _ioOption: IOOption? = null
    private var _exportDialog: Dialog? = null

    private enum class IOOption {
        eExport,
        eImport
    }

    private enum class TourbookType {
        eAscends,
        eBotches,
        eProjects
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tourbook)
        this.title = "Begehungen"

        _db = AppDatabase.getAppDatabase(this)
        _exporter = TourbookExporter(_db!!)
        _tourbookType = TourbookType.eAscends

        _initYears()
        prepareExportDialog()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            Toast.makeText(this, "Begehung gelöscht", Toast.LENGTH_SHORT).show()
            _displayContent(_availableYears!![_currentYearIdx])
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (FilesystemUtils.permissionGranted(grantResults)) {
            _exportDialog!!.show()
        } else {
            Toast.makeText(this, "Export/Import nicht möglich ohne Schreibrechte", Toast.LENGTH_SHORT).show()
        }
    }

    fun home(v: View) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    fun goToNextYear(v: View) {
        if (++_currentYearIdx <= _maxYearIdx) {
            findViewById<View>(R.id.prevYearButton).visibility = View.VISIBLE
            findViewById<View>(R.id.nextYearButton).visibility = if (_currentYearIdx == _maxYearIdx) View.INVISIBLE else View.VISIBLE
            _displayContent(_availableYears!![_currentYearIdx])
        }
    }

    fun goToPreviousYear(v: View) {
        if (--_currentYearIdx >= 0) {
            findViewById<View>(R.id.nextYearButton).visibility = View.VISIBLE
            findViewById<View>(R.id.prevYearButton).visibility = if (_currentYearIdx == 0) View.INVISIBLE else View.VISIBLE
            _displayContent(_availableYears!![_currentYearIdx])
        }
    }

    fun export(v: View) {
        if (!FilesystemUtils.isExternalStorageAvailable) {
            Toast.makeText(_exportDialog!!.context, "Speichermedium nicht verfügbar", Toast.LENGTH_SHORT).show()
            return
        }
        if (!FilesystemUtils.hasPermissionToWriteToExternalStorage(this@TourbookActivity)) {
            ActivityCompat.requestPermissions(this@TourbookActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
            return
        }
        _exportDialog!!.show()
    }

    fun showAscends(v: View) {
        title = "Begehungen"
        _tourbookType = TourbookType.eAscends
        _initYears()
    }

    fun showBotches(v: View) {
        title = "Säcke"
        _tourbookType = TourbookType.eBotches
        _initYears()
    }

    fun showProjects(v: View) {
        title = "Projekte"
        _tourbookType = TourbookType.eProjects
        _initYears()
    }

    private fun _displayContent(year: Int) {
        val ascends = if (_tourbookType == TourbookType.eAscends)
            _db!!.ascendDao().getAllBelowStyleId(year, AscendStyle.eBOTCHED.id)
        else if (_tourbookType == TourbookType.eBotches)
            _db!!.ascendDao().getAll(year, AscendStyle.eBOTCHED.id)
        else
            _db!!.ascendDao().getAll(year, AscendStyle.ePROJECT.id)

        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        (findViewById<View>(R.id.yearTextView) as TextView).text = year.toString()

        var currentMonth: Int
        var currentDay: Int
        var currentRegionId: Int
        currentRegionId = -1
        currentDay = currentRegionId
        currentMonth = currentDay
        for (ascend in ascends) {
            val month = ascend.month
            val day = ascend.day

            var route = _db!!.routeDao().getRoute(ascend.routeId)
            val rock: Rock?
            val sector: Sector
            val region: Region
            if (route == null) {
                // The database entry has been deleted
                route = _db!!.createUnknownRoute()
                rock = _db!!.createUnknownRock()
                sector = _db!!.createUnknownSector()
                region = _db!!.createUnknownRegion()
            } else {
                rock = _db!!.rockDao().getRock(route.parentId)
                sector = _db!!.sectorDao().getSector(rock!!.parentId)
                region = _db!!.regionDao().getRegion(sector.parentId)
            }

            if (month != currentMonth || day != currentDay || region.id != currentRegionId) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "$day.$month.$year",
                        region.name,
                        WidgetUtils.infoFontSizeDp, null,
                        -0x444445,
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

            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    rock.name + " - " + route.name,
                    route.grade!!,
                    WidgetUtils.tableFontSizeDp,
                    onClickListener,
                    Color.WHITE,
                    Typeface.NORMAL))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    private fun _showFileChooser() {
        val defaultFileName = if (_ioOption == IOOption.eExport)
            _FILE_NAME
        else
            ""
        FileChooser(_exportDialog!!.context, defaultFileName).setFileListener(object : FileChooser.FileSelectedListener {
            override fun fileSelected(file: File) {
                val filePath = file.absolutePath
                if (_ioOption == IOOption.eImport && !file.exists()) {
                    Toast.makeText(_exportDialog!!.context, "Datei existiert nicht", Toast.LENGTH_SHORT).show()
                    return
                }
                _showConfirmDialog(filePath)
            }
        }).showDialog()
    }

    private fun _showConfirmDialog(filePath: String) {
        val confirmDialog = Dialog(_exportDialog!!.context)
        confirmDialog.setContentView(R.layout.dialog)
        val infoText = if (_ioOption == IOOption.eExport)
            "Dies überschreibt eine bereits vorhandene Datei gleichen Namens.\nTrotzdem exportieren?"
        else
            "Dies überschreibt das gesamte Tourenbuch.\nTrotzdem importieren?"
        (confirmDialog.findViewById<View>(R.id.dialogText) as TextView).text = infoText
        confirmDialog.findViewById<View>(R.id.yesButton).setOnClickListener {
            try {
                var successMsg = filePath
                if (_ioOption == IOOption.eExport) {
                    _exporter!!.exportTourbook(filePath)
                    successMsg += " erfolgreich exportiert"
                } else {
                    _exporter!!.importTourbook(filePath)
                    successMsg += " erfolgreich importiert"
                }
                Toast.makeText(this@TourbookActivity, successMsg, Toast.LENGTH_SHORT).show()
            } catch (e: JSONException) {
                Toast.makeText(this@TourbookActivity, "Fehler beim Export/Import", Toast.LENGTH_SHORT).show()
            }

            confirmDialog.dismiss()
            _exportDialog!!.dismiss()
            _initYears()
        }
        confirmDialog.findViewById<View>(R.id.noButton).setOnClickListener { confirmDialog.dismiss() }
        confirmDialog.setCanceledOnTouchOutside(false)
        confirmDialog.setCancelable(false)
        confirmDialog.show()
    }

    private fun _initYears() {
        _availableYears = when (_tourbookType) {
            TourbookType.eAscends -> _db!!.ascendDao().getYearsBelowStyleId(AscendStyle.eBOTCHED.id)
            TourbookType.eBotches -> _db!!.ascendDao().getYears(AscendStyle.eBOTCHED.id)
            else -> _db!!.ascendDao().getYears(AscendStyle.ePROJECT.id)
        }

        Arrays.sort(_availableYears)
        _maxYearIdx = _availableYears!!.size - 1
        _currentYearIdx = _maxYearIdx
        if (_currentYearIdx >= 0) {
            findViewById<View>(R.id.nextYearButton).visibility = View.INVISIBLE
            findViewById<View>(R.id.prevYearButton).visibility = if (_availableYears!!.size > 1) View.VISIBLE else View.INVISIBLE
            _displayContent(_availableYears!![_currentYearIdx])
        }
    }

    private fun prepareExportDialog() {
        _exportDialog = Dialog(this)
        _exportDialog!!.setContentView(R.layout.export_dialog)
        _ioOption = IOOption.eExport

        val exportRadioButton = _exportDialog!!.findViewById<View>(R.id.exportRadioButton) as RadioButton
        val importRadioButton = _exportDialog!!.findViewById<View>(R.id.importRadioButton) as RadioButton
        exportRadioButton.setOnClickListener {
            exportRadioButton.isChecked = true
            importRadioButton.isChecked = false
            _ioOption = IOOption.eExport
        }
        importRadioButton.setOnClickListener {
            exportRadioButton.isChecked = false
            importRadioButton.isChecked = true
            _ioOption = IOOption.eImport
        }
        _exportDialog!!.findViewById<View>(R.id.okButton).setOnClickListener { _showFileChooser() }
        _exportDialog!!.findViewById<View>(R.id.cancelButton).setOnClickListener { _exportDialog!!.dismiss() }
        _exportDialog!!.setCanceledOnTouchOutside(false)
        _exportDialog!!.setCancelable(false)
    }
}
