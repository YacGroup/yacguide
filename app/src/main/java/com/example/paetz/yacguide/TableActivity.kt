package com.example.paetz.yacguide

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.network.JSONWebParser
import com.example.paetz.yacguide.utils.NetworkUtils

abstract class TableActivity : BaseNavigationActivity(), UpdateListener {
    protected lateinit var db: AppDatabase
    protected var jsonParser: JSONWebParser? = null
    private var _updateDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getAppDatabase(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.table, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_download -> update()
            R.id.action_delete -> delete()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    open fun showComments(v: View) {}

    // UpdateListener
    override fun onEvent(success: Boolean) {
        _updateDialog?.dismiss()

        if (success) {
            Toast.makeText(this, "Bereich aktualisiert", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Fehler bei Aktualisierung", Toast.LENGTH_SHORT).show()
        }
        displayContent()
    }

    private fun update() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Keine Internetverbindung", Toast.LENGTH_LONG).show()
            return
        }
        if (jsonParser != null) {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog)
            (dialog.findViewById<View>(R.id.dialogText) as TextView).text = getString(R.string.dialog_question_update)
            dialog.findViewById<View>(R.id.yesButton).setOnClickListener {
                jsonParser?.fetchData()
                showUpdateDialog()
                dialog.dismiss()
            }
            dialog.findViewById<View>(R.id.noButton).setOnClickListener { dialog.dismiss() }
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }
    }

    fun delete() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog)
        (dialog.findViewById<View>(R.id.dialogText) as TextView).text = getString(R.string.dialog_question_delete)
        dialog.findViewById<View>(R.id.yesButton).setOnClickListener {
            deleteContent()
            dialog.dismiss()
            Toast.makeText(applicationContext, "Bereich gelöscht", Toast.LENGTH_SHORT).show()
            displayContent()
        }
        dialog.findViewById<View>(R.id.noButton).setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun showUpdateDialog() {
        _updateDialog = Dialog(this)
        _updateDialog?.setContentView(R.layout.info_dialog)
        _updateDialog?.setCancelable(false)
        _updateDialog?.setCanceledOnTouchOutside(false)
        _updateDialog?.show()
    }

    protected fun prepareCommentDialog(): Dialog {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.comment_dialog)
        dialog.findViewById<View>(R.id.closeButton).setOnClickListener { dialog.dismiss() }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()

        return dialog
    }

    protected abstract fun displayContent()

    protected abstract fun deleteContent()
}
