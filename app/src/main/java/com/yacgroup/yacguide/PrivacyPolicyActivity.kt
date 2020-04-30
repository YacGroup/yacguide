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
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme

class PrivacyPolicyActivity : BaseNavigationActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_policy
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = getString(R.string.privacy_policy)

        displayContent()
    }

    private fun displayContent() {
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
        val privacyTextView = findViewById<TextView>(R.id.textView)
        markwon.setMarkdown(privacyTextView, privacyStr)
    }
}