/*
 * Copyright (C) 2022 Christian Sommer
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
 * maybe become obsolete.
 */

package com.yacgroup.yacguide

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.yacgroup.yacguide.utils.ActivityUtils

class AboutLibrariesActivity : AppCompatActivity () {

    private lateinit var _activityUtils: ActivityUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activityUtils = ActivityUtils(this)
        setTitle(R.string.open_source_libraries)
        setContentView(R.layout.activity_about_libraries)
        _displaycontent()
    }

    private fun _displaycontent() {
        // The resource JSON file is automatically generated
        // by the Gradle plugin com.mikepenz.aboutlibraries.plugin
        val jsonStr = resources.openRawResource(R.raw.aboutlibraries)
            .bufferedReader().use  { it.readText() }
        val libs = Libs.Builder()
            .withJson(jsonStr)
            .build()
        for (lib in libs.libraries.sortedBy { it.uniqueId }) {
            _createEntry(lib)
        }
    }

    private fun _createEntry(lib: Library) {
        layoutInflater.inflate(R.layout.about_libraries_entry, null).let {
            val content = findViewById<LinearLayout>(R.id.aboutLibrariesContent)
            it.setOnClickListener { _activityUtils.openUrl(lib.website.orEmpty()) }
            it.findViewById<TextView>(R.id.aboutLibrariesName).text = lib.uniqueId
            it.findViewById<TextView>(R.id.aboutLibrariesVersion).text = lib.artifactVersion
            val licenses= lib.licenses.joinToString(
                separator=", ",
                transform = { license -> license.name }
            )
            it.findViewById<TextView>(R.id.aboutLibrariesLicenses).text = licenses
            content.addView(it)
        }
    }
}
