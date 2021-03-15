/*
 * Copyright (C) 2021 Christian Sommer
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

package com.yacgroup.yacguide.utils

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider.getUriForFile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

import com.yacgroup.yacguide.R

class ContactUtils(activity: AppCompatActivity) : ActivityUtils(activity) {

    private val _sdkVersion: Int = android.os.Build.VERSION.SDK_INT
    private val _androidRelease: String = android.os.Build.VERSION.RELEASE

    /*
     * Send an email, if an tour book import error occurs.
     */
    fun reportImportError(errMsg: String, jsonFile: Uri) {
        _errMsgToUri(errMsg, "import_error")?.let { logFile ->
            _sendBugReport(
                    "Fehler beim Tourenbuch-Import",
                    "Bei mir tritt ein Fehler beim Import des angehängten Tourenbuchs auf.",
                    arrayListOf(logFile, jsonFile)
            )
        }
    }

    /*
     * Send a general bug report.
     */
    fun sendBugReport() {
        _sendBugReport(
                "Fehler ...",
                "Bitte den Fehler hier so genau wie möglich beschreiben."
        )
    }

    /*
     * Send a general feedback email.
     */
    fun sendFeedback() {
        _sendEmail()
    }

    private fun _sendBugReport(subject: String, bugDescr: String,
                               attachments: ArrayList<Uri>? = null) {
        val emailBody = """
            |Hallo YacGuide Team,
            |
            |$bugDescr
            |
            |App Version: $appVersion
            |Android SDK: $_sdkVersion ($_androidRelease)
            |
            |Grüße""".trimMargin()
        _sendEmail(subject, emailBody, attachments)
    }

    private fun _sendEmail(subject: String? = null, body: String? = null,
                           attachments: ArrayList<Uri>? = null ) {
        val emailIndent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(activity.getString(R.string.contact_email_address)))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            // Grant temporary access to the internal directories specified by the content provider.
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments)
        }
        if (emailIndent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(emailIndent)
        } else {
            Toast.makeText(activity, R.string.no_email_app_available, Toast.LENGTH_SHORT).show()
        }
    }

    /*
     * Save error message to a file with the given prefix and return URI to this file.
     */
    private fun _errMsgToUri(errMsg: String, fileNamePrfx: String): Uri? {
        val logFileDir = File(activity.cacheDir, "logfiles").apply {
            mkdirs()
        }
        var fileUri: Uri? = null
        try {
            val calendar = Calendar.getInstance()
            val timestamp = SimpleDateFormat("yyyy-MM-dd.HH-mm-ss",
                    Locale.GERMAN).format(calendar.time)
            val logFile = File(logFileDir, "$fileNamePrfx.$timestamp.log")
            FileOutputStream(logFile).use {
                it.write(errMsg.toByteArray(Charsets.UTF_8))
            }
            /* We have to use the content provider at this point because otherwise apps cannot
             * access the internal app directories.
             */
            fileUri = getUriForFile(
                    activity,
                    activity.packageName.plus(".fileprovider"),
                    logFile)
        } catch (e: Exception) {
            Toast.makeText(activity, activity.getString(R.string.cannot_create_logfile),
                    Toast.LENGTH_LONG).show()
        }
        return fileUri
    }
}
