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
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class AboutActivity : BaseNavigationActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_about
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = getString(R.string.menu_about, getString(R.string.app_name))

        _createEntry(getString(R.string.data_src), getString(R.string.data_src_url),null)
        _createEntry(getString(R.string.privacy_policy), null) { _showPrivacyPolicy() }
        _createEntry(
                getString(R.string.source_code_and_support),
                getString(R.string.github_url),
                null )
        _createEntry(getString(R.string.license), getString(R.string.license_url),null)
        _createEntry(getString(R.string.version), _getAppVersion(), null)
    }

    private fun _createEntry(
            title: String,
            description: String?,
            callback: ((view: View) -> Unit)?) {
        val contentAbout: LinearLayout = findViewById(R.id.aboutContent)
        val entry = layoutInflater.inflate(R.layout.about_entry, null)
        if (callback == null && URLUtil.isValidUrl(description)) {
            entry.setOnClickListener { description?.let { url -> _openUrl(url) } }
        } else {
            entry.setOnClickListener(callback)
        }
        entry.findViewById<TextView>(R.id.aboutEntryTitle).text = title
        entry.findViewById<TextView>(R.id.aboutEntryDescription).text = description
        contentAbout.addView(entry)
    }

    private fun _getAppVersion(): String {
        try {
            val pkgManager = this.packageManager
            val pkgInfo = pkgManager.getPackageInfo(this.packageName, 0)
            return pkgInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "0.0"
    }

    private fun _showPrivacyPolicy() {
        val intent = Intent(this, PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }

    private fun _openUrl(url: String) {
        val openURL = Intent(android.content.Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)
        if (openURL.resolveActivity(packageManager) != null) {
            startActivity(openURL)
        } else {
            Toast.makeText(this, R.string.no_webbrowser_available, Toast.LENGTH_SHORT).show()
        }
    }
}
