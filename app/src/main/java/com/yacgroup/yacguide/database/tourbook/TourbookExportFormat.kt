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

package com.yacgroup.yacguide.database.tourbook

/*
 * Normally the mapping between MIME type and extension is provided by library
 * android.webkit.MimeTypeMap.
 * The library seems to have a bug in API 26 and 27 (and maybe in other versions),
 * so that the file extension cannot be determined by MIME type "application/json".
 */
enum class TourbookExportFormat (val id: Int, val extension: String, val mimeType: String) {
    eJSON(0, "json", "application/json"),
    eJSONVERBOSE(1, "json", "application/json"),
    eCSV(2, "csv", "text/comma-separated-values");

    companion object {
        private val _by_id = HashMap<Int, TourbookExportFormat>()
        private val _extension_by_mimeType = HashMap<String, String>()

        init {
            values().forEach { _by_id[it.id] = it }
            values().forEach {
                _extension_by_mimeType[it.mimeType] = it.extension
            }
        }

        fun fromId(id: Int): TourbookExportFormat? = _by_id[id]

        fun getExtensionFromMimeType(mimeType: String): String? {
            return _extension_by_mimeType[mimeType]
        }
    }
}
