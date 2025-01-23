/*
 * Copyright (C) 2023, 2025 Christian Sommer
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

/*
 * NOTE:
 * The library can automatically generate a view.
 * However, it uses material design elements which makes things a bit inconsistent within
 * this app. As soon as the app is migrated to material design, this and related code
 * may become obsolete.
 */

package com.yacgroup.yacguide

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.yacgroup.yacguide.databinding.AboutLibrariesEntryBinding
import com.yacgroup.yacguide.databinding.ActivityAboutLibrariesBinding
import com.yacgroup.yacguide.utils.ActivityUtils

class AboutLibrariesActivity : AppCompatActivity() {

    private lateinit var _activityViewBinding: ActivityAboutLibrariesBinding
    private lateinit var _activityUtils: ActivityUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activityUtils = ActivityUtils(this)
        setTitle(R.string.software_and_licenses)
        _activityViewBinding = ActivityAboutLibrariesBinding.inflate(layoutInflater)
        setContentView(_activityViewBinding.root)
        _displayContent()
    }

    private fun _displayContent() {
        // The resource JSON file is automatically generated
        // by the Gradle plugin com.mikepenz.aboutlibraries.plugin
        val jsonStr = resources.openRawResource(R.raw.aboutlibraries)
            .bufferedReader().use  { it.readText() }
        val libs = Libs.Builder()
            .withJson(jsonStr)
            .build()
        libs.libraries.sortedBy { it.uniqueId }.forEach {
            _createEntry(it)
        }
    }

    private fun _createEntry(lib: Library) {
        _activityViewBinding.aboutLibrariesContent.addView(
            AboutLibrariesEntryBinding.inflate(layoutInflater).apply {
                root.setOnClickListener { _activityUtils.openUrl(lib.website.orEmpty()) }
                aboutLibrariesName.text = lib.uniqueId
                aboutLibrariesVersion.text = lib.artifactVersion
                val licenses = lib.licenses.joinToString(
                    separator = ", ",
                    transform = { license -> license.name }
                )
                aboutLibrariesLicenses.text = licenses
            }.root
        )
    }
}
