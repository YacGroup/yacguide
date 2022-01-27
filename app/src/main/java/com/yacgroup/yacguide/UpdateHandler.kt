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

package com.yacgroup.yacguide

import android.app.Dialog
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yacgroup.yacguide.network.JSONWebParser
import com.yacgroup.yacguide.utils.DialogWidgetBuilder
import com.yacgroup.yacguide.utils.NetworkUtils

class UpdateHandler(private val _activity: AppCompatActivity,
                    private var _jsonParser: JSONWebParser): UpdateListener {

    private var _updateDialog: Dialog
    private var _isRecurringUpdate: Boolean = false
    private var _success: Boolean = true
    private var _onUpdateFinished: () -> Unit = {}

    init {
        _jsonParser.listener = this

        _updateDialog = Dialog(_activity)
        _updateDialog.setContentView(R.layout.info_dialog)
        _updateDialog.setCancelable(false)
        _updateDialog.setCanceledOnTouchOutside(false)
    }

    override fun onUpdateStatus(statusMessage: String) {
        _activity.runOnUiThread {
            _updateDialog.findViewById<TextView>(R.id.dialogText).text = statusMessage
        }
    }

    override fun onUpdateFinished(success: Boolean) {
        _success = _success && success
        _onUpdateFinished()

        if (!_isRecurringUpdate) {
            finish()
        }
    }

    fun setJsonParser(parser: JSONWebParser) {
        _jsonParser = parser
        _jsonParser.listener = this
    }

    fun update(onUpdateFinished: () -> Unit = {}, isRecurring: Boolean = false) {
        _onUpdateFinished = onUpdateFinished
        _isRecurringUpdate = isRecurring
        if (!NetworkUtils.isNetworkAvailable(_activity)) {
            Toast.makeText(_activity, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
            return
        }
        _jsonParser.fetchData()
        _updateDialog.show()
    }

    fun delete(deleteContent: () -> Unit) {
        DialogWidgetBuilder(_activity, R.string.dialog_question_delete).apply {
            setIcon(android.R.drawable.ic_dialog_alert)
            setNegativeButton()
            setPositiveButton { _, _ ->
                deleteContent()
                Toast.makeText(
                    _activity,
                    R.string.region_deleted,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.show()
    }

    fun finish() {
        _updateDialog.dismiss()

        if (_success) {
            Toast.makeText(_activity, R.string.successful_refresh, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(_activity, R.string.error_on_refresh, Toast.LENGTH_SHORT).show()
        }

        _updateDialog.findViewById<TextView>(R.id.dialogText).setText(R.string.dialog_loading)
        _success = true
    }

    fun configureDownloadButton(enabled: Boolean, onUpdateFinished: () -> Unit) {
        val button = _activity.findViewById<ImageButton>(R.id.downloadButton)
        if (enabled) {
            button.visibility = View.VISIBLE
            button.setOnClickListener{
                update({ onUpdateFinished() })
            }
        } else {
            button.visibility = View.INVISIBLE
        }
    }
}