/*
 * Copyright (C) 2020 Axel Paetzold
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
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme

class AboutActivity : BaseNavigationActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_about
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = getString(R.string.menu_about, getString(R.string.app_name))

        val textViewAppVersion: TextView = findViewById(R.id.textViewAppVersion)
        textViewAppVersion.text = "Version ${getAppVersion()}"
        textViewAppVersion.gravity = Gravity.CENTER_HORIZONTAL
    }

    private fun getAppVersion() : String {
        try {
            val pkgManager = this.packageManager
            val pkgInfo = pkgManager.getPackageInfo(this.packageName, 0)
            return pkgInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "0.0"
    }

    @Suppress("UNUSED_PARAMETER")
    fun showPrivacyPolicy(v: View) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.comment_dialog)
        dialog.findViewById<TextView>(R.id.titleTextView).setText(R.string.privacy_policy)
        dialog.findViewById<View>(R.id.closeButton).setOnClickListener { dialog.dismiss() }

        val privacyTextView = _createPrivacyTextView(dialog.context)
        val layout = dialog.findViewById<LinearLayout>(R.id.commentLayout)
        layout.addView(privacyTextView)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun _createPrivacyTextView(context: Context): TextView {
        class MarkwonPlugin: AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                builder.headingBreakHeight(0)
            }
            override fun processMarkdown(markdown: String): String {
                // Replace front matter from markdown file by markdown section.
                val regex =  Regex("^---.*---$",
                        setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))
                return markdown.replace(regex, "# ".plus(getString(R.string.privacy_policy)))
            }
        }
        val markwon = Markwon.builder(context).usePlugin(MarkwonPlugin()).build()
        val rawResource = getResources().openRawResource(R.raw.privacy_policy)
        val privacyStr = rawResource.bufferedReader().use  { it.readText() }
        val privacyTextView = TextView(context)
        markwon.setMarkdown(privacyTextView, privacyStr)
        privacyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10.toFloat())

        return privacyTextView
    }
}
