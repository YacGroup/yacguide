/*
 * Copyright (C) 2020 Christian Sommer
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

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yacgroup.yacguide.markwon.WhatsNewMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import java.io.File

class WhatsNewInfo(private var _activity: AppCompatActivity) {

    companion object {
        private const val _RELEASE_NOTES_PATH = "release-notes"
    }

    private var _sharedPrefs: SharedPreferences
    private var _numReleaseNotesShow: Int = 1
    private var _releaseNoteFiles: Array<String>?
    private var _markwonBuilder: Markwon
    private  lateinit var _view: View

    init {
        _activity.let {
            _markwonBuilder = Markwon.builder(it)
                    .usePlugin(WhatsNewMarkwonPlugin(it.getString(R.string.github_url)))
                    .usePlugin(LinkifyPlugin.create())
                    .build()
            _releaseNoteFiles = it.assets.list(_RELEASE_NOTES_PATH)
            _sharedPrefs = it.getSharedPreferences(
                    it.getString(R.string.preferences_filename),
                    Context.MODE_PRIVATE)
        }
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
    fun showDialog() {
        _numReleaseNotesShow = 1
        _activity.let {
            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater
            // Pass null as the parent view because its going in the dialog layout.
            _view = inflater.inflate(R.layout.whats_new, null)
            _view.findViewById<TextView>(R.id.whatsNewShowMore).apply {
                text = it.getString(R.string.more).toUpperCase()
                isClickable = true
                setOnClickListener { _showMore() }
            }
            _setTextView()
            builder.apply {
                setTitle(_activity.getString(R.string.whats_new) + " ...")
                setView(_view)
                setPositiveButton(it.getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                }
            }
            builder.show()
        }
    }

    /*
     * Read release notes from Markdown files, add an additional heading for each release
     * and merge them together. Show only a limited number of versions.
     */
    private fun _getReleaseNotes(): String {
        val releaseNotes = StringBuilder()
        _releaseNoteFiles?.let {
            for ((idx, name) in it.sortedDescending().withIndex()) {
                val file = File(_RELEASE_NOTES_PATH, name)
                releaseNotes.let {
                    it.append("# Version ")
                    it.appendLine(name.removePrefix("v").removeSuffix(".md"))
                    it.append(_activity.assets.open(file.path).bufferedReader().use { it.readText() })
                }
                // Limit the number of versions to avoid that the dialog becomes too long.
                if (idx == _numReleaseNotesShow - 1) break
            }
        }
        return releaseNotes.toString()
    }

    /*
     * Apply rendered Markdown to text view
     */
    private fun _setTextView() {
        _markwonBuilder.setMarkdown(
                _view.findViewById<TextView>(R.id.whatsNewTextView),
                _getReleaseNotes()
        )
    }

    /*
     * Show one more release note, with every call.
     */
    private fun _showMore() {
        _numReleaseNotesShow++
        _setTextView()
        // Scroll down
        _view.findViewById<ScrollView>(R.id.whatsNewScrollView).let {
            it.post(Runnable { it.fullScroll(ScrollView.FOCUS_DOWN) })
        }
        // If last release note is shown, disable the "button"
        _releaseNoteFiles?.let {
            if (_numReleaseNotesShow == it.size) {
                _view.findViewById<TextView>(R.id.whatsNewShowMore).visibility = View.INVISIBLE
            }
        }
    }
}
