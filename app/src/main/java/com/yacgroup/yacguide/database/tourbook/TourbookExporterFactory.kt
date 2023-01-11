/*
 * Copyright (C) 2023 Christian Sommer
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
import com.yacgroup.yacguide.database.DatabaseWrapper
import kotlin.reflect.full.createInstance

class TourbookExporterFactory(
    private val _db: DatabaseWrapper,
    private val _contentResolver: ContentResolver) {

    fun create(format: TourbookExportFormat): BaseExporter {
        return when (format) {
            TourbookExportFormat.eJSON -> JsonExporter::class
            TourbookExportFormat.eJSONVERBOSE -> JsonVerboseExporter::class
            TourbookExportFormat.eCSV -> CsvExporter::class
        }.createInstance().apply {
            db = _db
            contentResolver = _contentResolver
        }
    }
}
