package com.example.paetz.yacguide.utils

import android.app.Dialog
import android.content.Context
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.example.paetz.yacguide.R

import java.io.File
import java.util.Arrays

class FileChooser(private val context: Context, defaultFileName: String) {

    private val parentDir = ".."
    private val list: ListView
    private val dialog: Dialog
    private var currentPath: File? = null
    private var fileListener: FileSelectedListener? = null

    interface FileSelectedListener {
        fun fileSelected(file: File)
    }

    fun setFileListener(fileListener: FileSelectedListener): FileChooser {
        this.fileListener = fileListener
        return this
    }

    init {
        currentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        dialog = Dialog(context)
        dialog.setContentView(R.layout.choose_file_dialog)

        val fileNameEditText = dialog.findViewById<View>(R.id.fileNameEditText) as EditText
        list = dialog.findViewById<View>(R.id.filesListView) as ListView
        list.onItemClickListener = AdapterView.OnItemClickListener { _, _, which, _ ->
            val chosenFile = getChosenFile(list.getItemAtPosition(which) as String)
            if (chosenFile.isDirectory) {
                displayContent(chosenFile)
            } else {
                fileNameEditText.setText(chosenFile.name)
            }
        }
        dialog.findViewById<View>(R.id.okButton).setOnClickListener(View.OnClickListener {
            var fileName = fileNameEditText.text.toString()
            if (fileName.isEmpty()) {
                Toast.makeText(dialog.context, "Keine Datei ausgewählt", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            } else if (!fileName.toLowerCase().endsWith(".json")) {
                fileName += ".json"
                Toast.makeText(dialog.context, "Dateisuffix \".json\" automatisch angefügt", Toast.LENGTH_SHORT).show()
            }
            if (fileListener != null) {
                fileListener!!.fileSelected(File(currentPath, fileName))
            }
            dialog.dismiss()
        })
        dialog.findViewById<View>(R.id.cancelButton).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.chooseFileButton).setOnClickListener { displayContent(currentPath) }

        dialog.findViewById<TextView>(R.id.currentPathTextView).text = "Pfad: " + currentPath!!.absolutePath
        fileNameEditText.setText(defaultFileName)
    }

    fun showDialog() {
        dialog.show()
    }

    private fun displayContent(path: File?) {
        currentPath = path
        if (!currentPath!!.exists()) {
            currentPath!!.mkdirs()
        }
        val dirs = currentPath!!.listFiles { file -> file.canRead() && file.isDirectory }
        val files = currentPath!!.listFiles { file -> file.canRead() && !file.isDirectory && file.name.toLowerCase().endsWith(".json") }

        var i = 0
        val fileList: Array<String>
        if (currentPath!!.parentFile == null || currentPath!!.parentFile.listFiles() == null) {
            fileList = Array(dirs.size + files.size) {""}
        } else {
            fileList = Array(dirs.size + files.size + 1) {""}
            fileList[i++] = parentDir
        }
        Arrays.sort(dirs)
        Arrays.sort(files)
        for (dir in dirs) {
            fileList[i++] = dir.name
        }
        for (file in files) {
            fileList[i++] = file.name
        }

        dialog.findViewById<TextView>(R.id.currentPathTextView).text = "Pfad: " + currentPath!!.absolutePath
        list.adapter = object : ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, fileList) {
            override fun getView(pos: Int, view: View?, parent: ViewGroup): View {
                val result = super.getView(pos, view, parent) as TextView
                result.setSingleLine(true)
                return result
            }
        }
    }

    private fun getChosenFile(fileName: String): File {
        return if (fileName == parentDir) {
            currentPath!!.parentFile
        } else {
            File(currentPath, fileName)
        }
    }
}
