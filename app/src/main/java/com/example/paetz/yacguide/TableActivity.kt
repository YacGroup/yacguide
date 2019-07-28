package com.example.paetz.yacguide

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.network.JSONWebParser
import com.example.paetz.yacguide.utils.NetworkUtils

abstract class TableActivity : AppCompatActivity(), UpdateListener {

    protected lateinit var db: AppDatabase
    protected var jsonParser: JSONWebParser? = null
    protected var updateDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getAppDatabase(this)
    }

    protected fun initialize(layoutNumber: Int) {
        setContentView(layoutNumber)
    }

    // UpdateListener
    override fun onEvent(success: Boolean) {
        updateDialog?.dismiss()

        if (success) {
            Toast.makeText(this, "Bereich aktualisiert", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Fehler bei Aktualisierung", Toast.LENGTH_SHORT).show()
        }
        displayContent()
    }

    fun home(v: View) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    open fun back(v: View) {
        finish()
    }

    fun update(v: View) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Keine Internetverbindung", Toast.LENGTH_LONG).show()
            return
        }
        if (jsonParser != null) {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog)
            (dialog.findViewById<View>(R.id.dialogText) as TextView).text = "Diesen Bereich aktualisieren?"
            dialog.findViewById<View>(R.id.yesButton).setOnClickListener {
                jsonParser!!.fetchData()
                showUpdateDialog()
                dialog.dismiss()
            }
            dialog.findViewById<View>(R.id.noButton).setOnClickListener { dialog.dismiss() }
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }
    }

    fun delete(v: View) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog)
        (dialog.findViewById<View>(R.id.dialogText) as TextView).text = "Diesen Bereich löschen?"
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
        updateDialog = Dialog(this)
        updateDialog!!.setContentView(R.layout.info_dialog)
        updateDialog!!.setCancelable(false)
        updateDialog!!.setCanceledOnTouchOutside(false)
        updateDialog!!.show()
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
