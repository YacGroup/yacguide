/*
 * Copyright (C) 2021, 2023 Christian Sommer
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

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider.getUriForFile
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.extensions.queryIntentActivitiesCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ContactUtils(activity: AppCompatActivity) : ActivityUtils(activity) {

    private val _sdkVersion: Int = Build.VERSION.SDK_INT
    private val _androidRelease: String = Build.VERSION.RELEASE

    /**
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

    /**
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
        _openEmailIntent()
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
        _openEmailIntent(subject, emailBody, attachments)
    }

    /**
     * Create a custom chooser because there will be a lot of "useless" app answers
     * for for action = Intent.ACTION_SEND_MULTIPLE.
     */
    private fun _openEmailIntent(subject: String? = null, body: String? = null,
                                 attachments: ArrayList<Uri>? = null) {
        // Find all apps that can handle emails
        val possibleEmailIntents = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val appList = activity.packageManager.queryIntentActivitiesCompat(
                possibleEmailIntents, PackageManager.MATCH_DEFAULT_ONLY
        )
        if (appList.isNotEmpty()) {
            // Create a list of all target intents
            // but initialize them with our email intent.
            val targetIntents = appList.map {
                it.let {
                    val packageName = it.activityInfo.packageName
                    _getEmailIntent(subject, body, attachments).apply {
                        component = ComponentName(packageName, it.activityInfo.name)
                        setPackage(packageName)
                    }
                }
            }.toMutableList()
            val finalIntent = if (targetIntents.isNotEmpty()) {
                // Prepare an intent chooser with the first item in the list and add all other apps
                // as alternatives.
                val startIntent = targetIntents.removeAt(0)
                Intent.createChooser(
                        startIntent,
                        activity.getString(R.string.choose_email_app)).apply {
                    putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toTypedArray())
                }
            } else {
                // If everything above fails
                _getEmailIntent(subject, body, attachments)
            }
            activity.startActivity(finalIntent)
        } else {
            Toast.makeText(activity, R.string.no_email_app_available, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Create our email intent
     */
    private fun _getEmailIntent(subject: String? = null, body: String? = null,
                                attachments: ArrayList<Uri>? = null): Intent {
        return Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(activity.getString(R.string.contact_email_address)))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            // Grant temporary access to the internal directories specified by the content provider.
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments)
        }
    }

    /**
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
