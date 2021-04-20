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

import android.app.Dialog
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*

import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.network.JSONWebParser
import com.yacgroup.yacguide.utils.IntentConstants
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
            R.id.action_search -> _searchRockGlobally()
            R.id.action_download -> update()
            R.id.action_delete -> _delete()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    // UpdateListener
    override fun onUpdateFinished(success: Boolean) {
        _updateDialog?.dismiss()

        if (success) {
            Toast.makeText(this, R.string.objects_refreshed, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.error_on_refresh, Toast.LENGTH_SHORT).show()
        }
        displayContent()
    }

    override fun onUpdateStatus(statusMessage: String) {
        runOnUiThread {
            _updateDialog?.findViewById<TextView>(R.id.dialogText)?.text = statusMessage
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun update(v: View = View(this)) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
            return
        }
        jsonParser?.fetchData()
        showUpdateDialog()
    }

    private fun _delete() {
        val builder = DialogWidgetBuilder(this).apply {
            setTitle(R.string.dialog_question_delete)
            setIcon(android.R.drawable.ic_dialog_alert)
            setNegativeButton()
            setPositiveButton(R.string.ok) { _, _ ->
                deleteContent()
                Toast.makeText(
                        this@UpdatableTableActivity,
                        R.string.objects_deleted,
                        Toast.LENGTH_SHORT
                ).show()
                displayContent()
            }
        }
        builder.show()
    }

    private fun showUpdateDialog() {
        _updateDialog = Dialog(this)
        _updateDialog?.setContentView(R.layout.info_dialog)
        _updateDialog?.setCancelable(false)
        _updateDialog?.setCanceledOnTouchOutside(false)
        _updateDialog?.show()
    }

    protected fun displayDownloadButton() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        val downloadButton = layoutInflater.inflate(R.layout.button_download, null) as ImageButton
        layout.addView(downloadButton)
    }

    protected abstract fun deleteContent()

    protected abstract fun searchRocks(rockName: String): List<Rock>

    private fun _searchRockGlobally() {
        val searchDialog = Dialog(this)
        searchDialog.setContentView(R.layout.search_dialog)
        searchDialog.findViewById<Button>(R.id.searchButton).setOnClickListener {
            val rockName = searchDialog.findViewById<EditText>(R.id.dialogEditText).text.toString()
            if (rockName.isEmpty()) {
                Toast.makeText(searchDialog.context, R.string.hint_no_name, Toast.LENGTH_SHORT).show()
            } else {
                val rocks = searchRocks(rockName)
                if (rocks.isEmpty()) {
                    Toast.makeText(searchDialog.context, R.string.hint_name_not_found, Toast.LENGTH_SHORT).show()
                } else {
                    val rockIds = ArrayList(rocks.map{ it.id })
                    val intent = Intent(this, SelectedRockActivity::class.java)
                    intent.putIntegerArrayListExtra(IntentConstants.SELECTED_ROCK_IDS, rockIds)
                    startActivity(intent)
                    searchDialog.dismiss()
                }
            }
        }
        searchDialog.findViewById<Button>(R.id.cancelButton).setOnClickListener { searchDialog.dismiss() }
        searchDialog.setCancelable(false)
        searchDialog.setCanceledOnTouchOutside(false)
        searchDialog.show()
    }
}
