/*
 * Copyright (C) 2021, 2022, 2023 Axel Paetzold
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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yacgroup.yacguide.network.ExitCode
import com.yacgroup.yacguide.network.JSONWebParser
import com.yacgroup.yacguide.utils.DialogWidgetBuilder
import com.yacgroup.yacguide.utils.NetworkUtils

class UpdateHandler(private val _activity: AppCompatActivity,
                    private var _jsonParser: JSONWebParser): UpdateListener {

    private val _failedRegionsNames: MutableSet<String> = emptySet<String>().toMutableSet()
    private var _updateDialog: Dialog
    private var _isRecurringUpdate: Boolean = false
    private var _isSilentUpdate: Boolean = false
    private var _climbingObjectUId: ClimbingObjectUId = ClimbingObjectUId(0, "")
    private var _exitCode: ExitCode = ExitCode.SUCCESS
    private var _onUpdateFinished: () -> Unit = {}

    init {
        _jsonParser.listener = this

        _updateDialog = Dialog(_activity).apply {
            setContentView(R.layout.info_dialog)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
        _updateDialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            abort()
        }
    }

    override fun onUpdateStatus(statusMessage: String) {
        if (!_isSilentUpdate) {
            _activity.runOnUiThread {
                _updateDialog.findViewById<TextView>(R.id.dialogText).text = "${_climbingObjectUId.name} $statusMessage"
            }
        }
    }

    override fun onUpdateError(errorMessage: String)  {
        _failedRegionsNames.add(errorMessage)
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

    fun update(climbingObjectUId: ClimbingObjectUId = ClimbingObjectUId(0, ""),
               onUpdateFinished: () -> Unit = {},
               isRecurring: Boolean = false,
               isSilent: Boolean = false) {
        _climbingObjectUId = climbingObjectUId
        _onUpdateFinished = onUpdateFinished
        _isRecurringUpdate = isRecurring
        _isSilentUpdate = isSilent
        if (!NetworkUtils.isNetworkAvailable(_activity) && !_isSilentUpdate) {
            Toast.makeText(_activity, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
            return
        }
        _jsonParser.fetchData(climbingObjectUId)
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
                ExitCode.ERROR -> {
                    _showErrorDialog()
                    R.string.refresh_failed
                }
                else -> R.string.refresh_successful
            }
            Toast.makeText(_activity, finishMsgResource, Toast.LENGTH_SHORT).show()

            _updateDialog.findViewById<TextView>(R.id.dialogText).setText(R.string.dialog_loading)
        }

        _exitCode = ExitCode.SUCCESS
        _failedRegionsNames.clear()
    }

    fun configureDownloadButton(enabled: Boolean, climbingObjectUId: ClimbingObjectUId, onUpdateFinishedCallback: () -> Unit) {
        val button = _activity.findViewById<ImageButton>(R.id.downloadButton)
        if (enabled) {
            button.visibility = View.VISIBLE
            button.setOnClickListener{
                update(
                    climbingObjectUId = climbingObjectUId,
                    onUpdateFinished = onUpdateFinishedCallback)
            }
        } else {
            button.visibility = View.GONE
        }
    }

    private fun _showErrorDialog() {
        val view = _activity.layoutInflater.inflate(R.layout.comment_dialog, null)
        val dialog = DialogWidgetBuilder(_activity, R.string.dialog_text_failed_regions).apply {
            setView(view)
            setPositiveButton{ _, _ -> }
        }.create()
        val commentLayout = view.findViewById<LinearLayout>(R.id.commentLayout)
        _failedRegionsNames.forEach { regionName ->
            dialog.layoutInflater.inflate(R.layout.textview_info, commentLayout, false).let {
                it.findViewById<TextView>(R.id.infoTextView).text = regionName
                commentLayout.addView(it)
            }
        }
        dialog.show()
    }
}