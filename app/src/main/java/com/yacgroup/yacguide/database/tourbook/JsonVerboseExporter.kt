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

import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class JsonVerboseExporter: JsonExporter() {
    @Throws(IOException::class)
    override fun export(uri: Uri) {
        val jsonAscends = JSONArray().apply {
            db.getAscends().forEach { put(JSONObject(TourbookEntryVerbose(it, db).asMap())) }
        }
        writeStrToUri(uri, jsonAscends.toString(JSON_INDENT_SPACE))
    }
}
