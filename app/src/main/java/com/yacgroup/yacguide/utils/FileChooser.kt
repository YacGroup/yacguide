package com.yacgroup.yacguide.utils

import android.app.Dialog
import android.content.Context
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.yacgroup.yacguide.R

import java.io.File
import java.util.Arrays

class FileChooser(private val _context: Context, defaultFileName: String) {

    private val _parentDir = ".."
    private val _list: ListView
    private val _dialog: Dialog
    private var _currentPath: File
    private var _fileListener: FileSelectedListener? = null

    interface FileSelectedListener {
        fun fileSelected(file: File)
    }

    fun setFileListener(fileListener: FileSelectedListener): FileChooser {
        this._fileListener = fileListener
        return this
    }

    init {
        _currentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        _dialog = Dialog(_context)
        _dialog.setContentView(R.layout.choose_file_dialog)

        val fileNameEditText = _dialog.findViewById<EditText>(R.id.fileNameEditText)
        _list = _dialog.findViewById(R.id.filesListView)
        _list.onItemClickListener = AdapterView.OnItemClickListener { _, _, which, _ ->
            val chosenFile = _getChosenFile(_list.getItemAtPosition(which) as String)
            if (chosenFile.isDirectory) {
                _displayContent(chosenFile)
            } else {
                fileNameEditText.setText(chosenFile.name)
            }
        }
        _dialog.findViewById<View>(R.id.okButton).setOnClickListener(View.OnClickListener {
            var fileName = fileNameEditText.text.toString()
            if (fileName.isEmpty()) {
                Toast.makeText(_dialog.context, "Keine Datei ausgewählt", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            } else if (!fileName.toLowerCase().endsWith(".json")) {
                fileName += ".json"
                Toast.makeText(_dialog.context, "Dateisuffix \".json\" automatisch angefügt", Toast.LENGTH_SHORT).show()
            }

            _fileListener?.fileSelected(File(_currentPath, fileName))

            _dialog.dismiss()
        })
        _dialog.findViewById<View>(R.id.cancelButton).setOnClickListener { _dialog.dismiss() }
        _dialog.findViewById<View>(R.id.chooseFileButton).setOnClickListener { _displayContent(_currentPath) }

        _dialog.findViewById<TextView>(R.id.currentPathTextView).text = "Pfad: ${_currentPath.absolutePath}"
        fileNameEditText.setText(defaultFileName)
    }

    fun showDialog() {
        _dialog.show()
    }

    private fun _displayContent(path: File) {
        _currentPath = path
        if (!_currentPath.exists()) {
            _currentPath.mkdirs()
        }
        val dirs = _currentPath.listFiles { file -> file.canRead() && file.isDirectory }
        val files = _currentPath.listFiles { file -> file.canRead() && !file.isDirectory && file.name.toLowerCase().endsWith(".json") }

        var i = 0
        val fileList: Array<String>
        if (_currentPath.parentFile?.listFiles() == null) {
            fileList = Array(dirs.size + files.size) {""}
        } else {
            fileList = Array(dirs.size + files.size + 1) {""}
            fileList[i++] = _parentDir
        }
        Arrays.sort(dirs)
        Arrays.sort(files)
        for (dir in dirs) {
            fileList[i++] = dir.name
        }
        for (file in files) {
            fileList[i++] = file.name
        }

        _dialog.findViewById<TextView>(R.id.currentPathTextView).text = "Pfad: ${_currentPath.absolutePath}"
        _list.adapter = object : ArrayAdapter<String>(_context,
                android.R.layout.simple_list_item_1, fileList) {
            override fun getView(pos: Int, view: View?, parent: ViewGroup): View {
                val result = super.getView(pos, view, parent) as TextView
                result.setSingleLine(true)
                return result
            }
        }
    }

    private fun _getChosenFile(fileName: String): File {
        return if (fileName == _parentDir) {
            _currentPath.parentFile
        } else {
            File(_currentPath, fileName)
        }
    }
}
