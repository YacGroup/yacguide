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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.privacy_policy)
        setContentView(R.layout.activity_privacy_policy)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        _displayContent()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun _displayContent() {
        class MyMarkwonPlugin: AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                // Set default heading break height to 0.
                builder.headingBreakHeight(0)
            }
            override fun processMarkdown(markdown: String): String {
                // Repace front matter from markdown file by markdown section.
                val regex =  Regex("^---.*---$",
                        setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))
                return markdown.replace(regex, "# ".plus(getString(R.string.privacy_policy)))
            }
        }
        val markwon = Markwon.builder(this).usePlugin(MyMarkwonPlugin()).build()
        val rawResource = getResources().openRawResource(R.raw.privacy_policy)
        val privacyStr = rawResource.bufferedReader().use  { it.readText() }
        val privacyTextView = findViewById<TextView>(R.id.privacyPolicyTextView)
        markwon.setMarkdown(privacyTextView, privacyStr)
    }
}