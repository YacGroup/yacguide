/*
 * Copyright (C) 2019, 2022 Axel Paetzold
 * Copyright (C) 2021 Christian Sommer
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

package com.yacgroup.yacguide.database.tourbook

import android.content.ContentResolver
import android.net.Uri
import com.yacgroup.yacguide.database.DatabaseWrapper
import org.json.JSONException
import java.io.IOException

class TourbookExporter(
    private val _db: DatabaseWrapper,
    private val _contentResolver: ContentResolver) {

    var exportFormat = TourbookExportFormat.eJSON

    @Throws(IOException::class)
    fun exportTourbook(uri: Uri) {
        when (exportFormat) {
            TourbookExportFormat.eJSON, TourbookExportFormat.eJSONVERBOSE -> {
                JsonExporter(_db, _contentResolver).export(uri, exportFormat)
            }
            TourbookExportFormat.eCSV -> {
                CsvExporter(_db, _contentResolver).export(uri, exportFormat)
            }
        }
    }

    @Throws(JSONException::class, IOException::class)
    fun importTourbook(uri: Uri) {
        JsonImporter(_db, _contentResolver).import(uri)
    }
}
