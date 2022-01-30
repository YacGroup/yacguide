/*
 * Copyright (C) 2020, 2022 Christian Sommer
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

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.yacgroup.yacguide.utils.ActivityUtils
import com.yacgroup.yacguide.utils.DialogWidgetBuilder
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import java.io.File

class WhatsNewInfo(private var _activity: AppCompatActivity) {

    companion object {
        private const val _RELEASE_NOTES_PATH = "release-notes"
    }

    private val _sharedPrefs: SharedPreferences
    private val _releaseNoteFiles: Array<String>?
    private val _markwonBuilder: Markwon
    private lateinit var _view: View
    private val _activityUtils: ActivityUtils

    init {
        _activity.let {
            _markwonBuilder = Markwon.builder(it)
                    .usePlugin(LinkifyPlugin.create())
                    .build()
            _releaseNoteFiles = it.assets.list(_RELEASE_NOTES_PATH)
            _sharedPrefs = it.getSharedPreferences(
                    it.getString(R.string.preferences_filename),
                    Context.MODE_PRIVATE)
            _activityUtils = ActivityUtils(it)
        }
    }

    fun getReleaseNotesUrl(): String {
        return _activity.getString(R.string.release_notes_url, _activityUtils.appVersion)
    }

    /*
     * Check whether the "What's New" dialog needs to be shown or not. This is decided by comparing
     * the version stored in the settings with the current app version.
     */
    fun checkForVersionUpdate(): Boolean {
        _activity.let {
            val savedVerCode = _sharedPrefs.getInt(
                    it.getString(R.string.preference_key_version_code), 0)
            val curVerCode = try {
                @Suppress("DEPRECATION")
                it.packageManager.getPackageInfo(it.packageName, 0).versionCode
            } catch (e: Exception) {
                0
            }
            return if (curVerCode > savedVerCode) {
                val sharedPrefsEditor = _sharedPrefs.edit().apply {
                    putInt(it.getString(R.string.preference_key_version_code), curVerCode)
                }
                sharedPrefsEditor.apply()
                true
            } else {
                false
            }
        }
    }

    /*
     * Render Markdown code and show dialog.
     */
    @SuppressLint("InflateParams")
    fun showDialog() {
        _activity.let {
            val inflater = it.layoutInflater
            // Pass null as the parent view because its going in the dialog layout.
            _view = inflater.inflate(R.layout.whats_new, null)
            _markwonBuilder.setMarkdown(
                _view.findViewById(R.id.whatsNewTextView),
                _getReleaseNotes(_activityUtils.appVersion)
            )
            DialogWidgetBuilder(it, it.getString(R.string.show_whats_new)).apply {
                setView(_view)
                setPositiveButton(R.string.yes) { _, _ ->
                    _activityUtils.openUrl(getReleaseNotesUrl())
                }
                setNegativeButton(R.string.no)
            }.show()
        }
    }

    private fun _getReleaseNotes(version: String): String {
        val releaseNotes = StringBuilder()
        _releaseNoteFiles?.let { files ->
            for (name in files) {
                val basename = name.removeSuffix(".md")
                if (basename == version) {
                    val file = File(_RELEASE_NOTES_PATH, name)
                    releaseNotes.let { note ->
                        note.clear()
                        note.append(_activity.assets.open(file.path).bufferedReader().readText())
                    }
                    break
                }
            }
        }
        return releaseNotes.toString()
    }
}
