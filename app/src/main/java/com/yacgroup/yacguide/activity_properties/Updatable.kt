/*
 * Copyright (C) 2021 Axel Paetzold
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

package com.yacgroup.yacguide.activity_properties

import android.app.Dialog
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.TableActivity
import com.yacgroup.yacguide.UpdateListener
import com.yacgroup.yacguide.network.JSONWebParser
import com.yacgroup.yacguide.utils.DialogWidgetBuilder
import com.yacgroup.yacguide.utils.NetworkUtils

class Updatable(private val _activity: TableActivity,
                private val _jsonParser: JSONWebParser,
                private val _deleteContent: () -> Unit) : ActivityProperty, UpdateListener {

    private var _updateDialog: Dialog? = null

    init {
        _jsonParser.listener = this
    }

    override fun getMenuGroupId() = R.id.group_update

    override fun onMenuAction(menuItemId: Int) {
        when (menuItemId) {
            R.id.action_download -> _update()
            R.id.action_delete -> _delete()
        }
    }

    override fun onUpdateFinished(success: Boolean) {
        _updateDialog?.dismiss()

        if (success) {
            Toast.makeText(_activity, R.string.objects_refreshed, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(_activity, R.string.error_on_refresh, Toast.LENGTH_SHORT).show()
        }
        _activity.displayContent()
    }

    override fun onUpdateStatus(statusMessage: String) {
        _activity.runOnUiThread {
            _updateDialog?.findViewById<TextView>(R.id.dialogText)?.text = statusMessage
        }
    }

    fun getDownloadButton(): ImageButton {
        val button = _activity.layoutInflater.inflate(R.layout.button_download, null) as ImageButton
        button.setOnClickListener{ _update() }
        return button
    }

    private fun _update() {
        if (!NetworkUtils.isNetworkAvailable(_activity)) {
            Toast.makeText(_activity, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
            return
        }
        _jsonParser.fetchData()
        _showUpdateDialog()
    }

    private fun _delete() {
        DialogWidgetBuilder(_activity, R.string.dialog_question_delete).apply {
            setIcon(android.R.drawable.ic_dialog_alert)
            setNegativeButton()
            setPositiveButton { _, _ ->
                _deleteContent()
                Toast.makeText(
                    _activity,
                    R.string.objects_deleted,
                    Toast.LENGTH_SHORT
                ).show()
                _activity.displayContent()
            }
        }.show()
    }

    private fun _showUpdateDialog() {
        _updateDialog = Dialog(_activity)
        _updateDialog?.setContentView(R.layout.info_dialog)
        _updateDialog?.setCancelable(false)
        _updateDialog?.setCanceledOnTouchOutside(false)
        _updateDialog?.show()
    }
}