/*
 * Copyright (C) 2021, 2022 Axel Paetzold
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
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yacgroup.yacguide.network.ExitCode
import com.yacgroup.yacguide.network.JSONWebParser
import com.yacgroup.yacguide.utils.DialogWidgetBuilder
import com.yacgroup.yacguide.utils.NetworkUtils

class UpdateHandler(private val _activity: AppCompatActivity,
                    private var _jsonParser: JSONWebParser): UpdateListener {

    private var _updateDialog: Dialog
    private var _isRecurringUpdate: Boolean = false
    private var _isSilentUpdate: Boolean = false
    private var _exitCode: ExitCode = ExitCode.SUCCESS
    private var _onUpdateFinished: () -> Unit = {}

    init {
        _jsonParser.listener = this

        _updateDialog = Dialog(_activity)
        _updateDialog.setContentView(R.layout.info_dialog)
        _updateDialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            abort()
        }
    }

    override fun onUpdateStatus(statusMessage: String) {
        if (!_isSilentUpdate) {
            _activity.runOnUiThread {
                _updateDialog.findViewById<TextView>(R.id.dialogText).text = statusMessage
            }
        }
    }

    override fun onUpdateFinished(exitCode: ExitCode) {
        if (exitCode == ExitCode.ABORT) {
            _exitCode = ExitCode.ABORT
            finish()
        } else {
            if (exitCode == ExitCode.ERROR) {
                _exitCode = ExitCode.ERROR
            }
            _onUpdateFinished()
            if (!_isRecurringUpdate) {
                finish()
            }
        }
    }

    fun setJsonParser(parser: JSONWebParser) {
        _jsonParser = parser
        _jsonParser.listener = this
    }

    fun update(onUpdateFinished: () -> Unit = {}, isRecurring: Boolean = false, isSilent: Boolean = false) {
        _onUpdateFinished = onUpdateFinished
        _isRecurringUpdate = isRecurring
        _isSilentUpdate = isSilent
        if (!NetworkUtils.isNetworkAvailable(_activity) && !_isSilentUpdate) {
            Toast.makeText(_activity, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
            return
        }
        _jsonParser.fetchData()
        if (!_isSilentUpdate) {
            _updateDialog.show()
        }
    }

    fun abort() {
        _jsonParser.abort()
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
        if (!_isSilentUpdate) {
            _updateDialog.dismiss()

            val finishMsgResource = when (_exitCode) {
                ExitCode.ABORT -> R.string.refresh_aborted
                ExitCode.ERROR -> R.string.refresh_failed
                else -> R.string.refresh_successful
            }
            Toast.makeText(_activity, finishMsgResource, Toast.LENGTH_SHORT).show()

            _updateDialog.findViewById<TextView>(R.id.dialogText).setText(R.string.dialog_loading)
        }

        _exitCode = ExitCode.SUCCESS
    }

    fun configureDownloadButton(enabled: Boolean, onUpdateFinished: () -> Unit) {
        val button = _activity.findViewById<ImageButton>(R.id.downloadButton)
        if (enabled) {
            button.visibility = View.VISIBLE
            button.setOnClickListener{
                update({ onUpdateFinished() })
            }
        } else {
            button.visibility = View.GONE
        }
    }
}