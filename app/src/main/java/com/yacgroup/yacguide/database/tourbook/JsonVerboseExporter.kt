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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class JsonVerboseExporter(
    contentResolver: ContentResolver,
    private val _db: DatabaseWrapper): JsonExporter(contentResolver, _db) {

    companion object {
        private const val _JSON_INDENT_SPACE = 2
    }

    /*
     * Overrides the whole export() instead of just ascend2Json(), since the verbose
     * entries are built from a single query joining the whole ascend hierarchy
     * (see AscendDao.getAscendsVerbose()) rather than from a plain Ascend.
     */
    override suspend fun export(uri: Uri) {
        withContext(Dispatchers.IO) {
            val partnerNames = _db.getPartnerNameMap()
            val jsonAscends = JSONArray().apply {
                _db.getAscendsVerbose().forEach {
                    put(JSONObject(TourbookEntryVerbose(it, partnerNames).asMap()))
                }
            }
            writeStrToUri(uri, jsonAscends.toString(_JSON_INDENT_SPACE))
        }
    }
}
