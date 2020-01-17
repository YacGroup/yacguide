package com.yacgroup.yacguide

import android.app.Dialog
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.yacgroup.yacguide.network.JSONWebParser
import com.yacgroup.yacguide.utils.NetworkUtils

abstract class UpdatableTableActivity : TableActivity(), UpdateListener {

    private var _updateDialog: Dialog? = null
    protected var jsonParser: JSONWebParser? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sync_delete, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_download -> update()
            R.id.action_delete -> delete()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

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

    private fun delete() {
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

    protected abstract fun deleteContent()
}
