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
import android.net.Uri
import com.yacgroup.yacguide.database.DatabaseWrapper
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.IOException

class CsvExporter(
    private val _db: DatabaseWrapper,
    _contentResolver: ContentResolver): BaseExporter(_contentResolver) {

    /*
     * See https://commons.apache.org/proper/commons-csv/apidocs/index.html
     */
    @Throws(IOException::class)
    override fun export(uri: Uri, exportFormat: TourbookExportFormat) {
        val writer = StringBuffer()
        val csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT).apply {
            setHeader(*TourbookEntryVerbose.keys().toTypedArray())
            setTrim(true)
        }.build()
        CSVPrinter(writer, csvFormat).apply {
            _db.getAscends().forEach {
                printRecord(TourbookEntryVerbose(it, _db).values())
            }
            flush()
            close()
            writeStrToUri(uri, writer.toString())
        }
    }
}
