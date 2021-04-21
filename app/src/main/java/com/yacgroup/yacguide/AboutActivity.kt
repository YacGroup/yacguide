/*
 * Copyright (C) 2020 Axel Paetzold
 *               2020 Christian Sommer
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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.yacgroup.yacguide.utils.ActivityUtils
import com.yacgroup.yacguide.utils.ContactUtils
import com.yacgroup.yacguide.utils.DialogWidgetBuilder

class AboutActivity : BaseNavigationActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_about
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = getString(R.string.menu_about, getString(R.string.app_name))

        _createEntry(getString(R.string.data_src), getString(R.string.data_src_url))
        _createEntry(getString(R.string.privacy_policy), callback = { _showPrivacyPolicy() })
        _createEntry(getString(R.string.source_code_and_support), getString(R.string.github_url))
        _createEntry(getString(R.string.license), getString(R.string.license_url))
        _createEntry(getString(R.string.whats_new),
                callback = { WhatsNewInfo(this).showDialog() })
        _createEntry(getString(R.string.contact), getString(R.string.contact_details),
                callback = { _selectContactConcern() })
        _createEntry(getString(R.string.version), ActivityUtils(this).appVersion)
    }

    private fun _createEntry(
            title: String,
            description: String = "",
            callback: (() -> Unit)? = null) {
        layoutInflater.inflate(R.layout.about_entry, null).let {
            val contentAbout = findViewById<LinearLayout>(R.id.aboutContent)
            it.setOnClickListener { callback?.invoke() ?: _openUrl(description) }
            it.findViewById<TextView>(R.id.aboutEntryTitle).text = title
            it.findViewById<TextView>(R.id.aboutEntryDescription).text = description
            contentAbout.addView(it)
        }
    }

    private fun _showPrivacyPolicy() {
        val intent = Intent(this, PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }

    private fun _openUrl(url: String) {
        if (URLUtil.isValidUrl(url)) {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(url)
            if (openURL.resolveActivity(packageManager) != null) {
                startActivity(openURL)
            } else {
                Toast.makeText(this, R.string.no_webbrowser_available, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun _selectContactConcern() {
        val contactInterface = ContactUtils(this)
        val builder = DialogWidgetBuilder(this, R.string.contact_concern).apply {
            setItems(R.array.contactConcerns) { _, which ->
                when (which) {
                    0 -> contactInterface.sendFeedback()
                    1 -> contactInterface.sendBugReport()
                }
            }
            setNegativeButton()
        }
        builder.show()
    }
}
