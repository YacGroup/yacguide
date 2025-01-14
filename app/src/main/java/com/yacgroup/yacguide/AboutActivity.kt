/*
 * Copyright (C) 2020 Axel Paetzold
 *               2020,2025 Christian Sommer
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
import android.os.Bundle
import com.yacgroup.yacguide.databinding.AboutEntryBinding
import com.yacgroup.yacguide.databinding.ActivityAboutBinding
import com.yacgroup.yacguide.utils.ActivityUtils
import com.yacgroup.yacguide.utils.ContactUtils
import com.yacgroup.yacguide.utils.DialogWidgetBuilder

class AboutActivity : BaseNavigationActivity<ActivityAboutBinding>() {

    private lateinit var _activityUtils: ActivityUtils

    override fun getViewBinding() = ActivityAboutBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = getString(R.string.menu_about, getString(R.string.app_name))
        _activityUtils = ActivityUtils(this)

        _createEntry(getString(R.string.data_src), getString(R.string.data_src_url))
        _createEntry(getString(R.string.privacy_policy), callback = { _showPrivacyPolicy() })
        _createEntry(getString(R.string.source_code_and_support), getString(R.string.github_url))
        _createEntry(getString(R.string.license), getString(R.string.license_url))
        _createEntry(
            getString(R.string.software_and_licenses),
            callback = { _showLibraries()}
        )
        _createEntry(
            getString(R.string.whats_new),
            WhatsNewInfo(this).getReleaseNotesUrl()
        )
        _createEntry(getString(R.string.contact), getString(R.string.contact_details),
                callback = { _selectContactConcern() })
        _createEntry(getString(R.string.version), ActivityUtils(this).appVersion)
    }

    private fun _createEntry(
            title: String,
            description: String = "",
            callback: (() -> Unit)? = null) {
        binding.aboutContent.addView(
            AboutEntryBinding.inflate(layoutInflater).apply {
                root.setOnClickListener { callback?.invoke() ?: _activityUtils.openUrl(description) }
                aboutEntryTitle.text = title
                aboutEntryDescription.text = description
            }.root
        )
    }

    private fun _showPrivacyPolicy() {
        startActivity(Intent(this, PrivacyPolicyActivity::class.java))
    }

    private fun _selectContactConcern() {
        val contactInterface = ContactUtils(this)
        DialogWidgetBuilder(this, R.string.contact_concern).apply {
            setItems(R.array.contactConcerns) { _, which ->
                when (which) {
                    0 -> contactInterface.sendFeedback()
                    1 -> contactInterface.sendBugReport()
                }
            }
            setNegativeButton()
        }.show()
    }

    private fun _showLibraries() {
        startActivity(Intent(this, AboutLibrariesActivity::class.java))
    }
}
