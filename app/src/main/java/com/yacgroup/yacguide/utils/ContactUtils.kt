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

    private var _sdkVersion: Int = android.os.Build.VERSION.SDK_INT
    private var _androidRelease: String = android.os.Build.VERSION.RELEASE

    fun reportImportError(errMsg: String, jsonFile: Uri) {
        val subject = "Fehler beim Tourenbuch-Import"

        val body = """
            |Hallo YacGuide Team,
            |
            |Bei mir tritt der folgende Fehler beim Import des angehängten Tourenbuchs auf:
            |
            |
            |App Version: $appVersion
            |Android SDK: $_sdkVersion ($_androidRelease)
            |
            |Grüße
            |""".trimMargin()
        val logFileDir = File(activity.cacheDir, "logfiles").apply {
            mkdirs()
        }
        try {
            val calendar = Calendar.getInstance()
            val timestamp = SimpleDateFormat("yyyy-MM-dd.HH-mm-ss",
                    Locale.GERMAN).format(calendar.time)
            val logFile = File(logFileDir, "import_error.$timestamp.log")
            FileOutputStream(logFile).use {
                it.write(errMsg.toByteArray(Charsets.UTF_8))
            }
            /* We have to use the content provider at this point because otherwise apps cannot
             * access the internal app directories.
             */
            val logFileContentUri = getUriForFile(
                    activity,
                    "com.yacgroup.yacguide.fileprovider",
                    logFile)
            _sendEmail(subject, body, arrayListOf(logFileContentUri, jsonFile))
        } catch (e: Exception) {
            Toast.makeText(activity, activity.getString(R.string.cannot_create_logfile),
                    Toast.LENGTH_LONG).show()
        }
    }

    fun reportBug() {
        val subject = "[Fehler] ..."
        val body = """
            |Hallo YacGuide Team,
            |
            |Bitte den Fehler hier so genau wie möglich beschreiben.
            |
            |App Version: $appVersion
            |Android SDK: $_sdkVersion ($_androidRelease)
            |
            |Grüße
            |""".trimMargin()
        _sendEmail(subject, body)
    }

    fun sendFeedback() {
        _sendEmail()
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
}
