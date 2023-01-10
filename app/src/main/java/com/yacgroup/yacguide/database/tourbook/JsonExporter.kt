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
import com.yacgroup.yacguide.database.Ascend
import com.yacgroup.yacguide.database.DatabaseWrapper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class JsonExporter(
    private val _db: DatabaseWrapper,
    _contentResolver: ContentResolver): BaseExporter(_contentResolver) {

    private val JSON_INDENT_SPACE = 2

    @Throws(IOException::class)
    override fun export(uri: Uri, exportFormat: TourbookExportFormat) {
        val jsonAscends = JSONArray()
        _db.getAscends().forEach {
            when (exportFormat) {
                TourbookExportFormat.eJSON -> jsonAscends.put(_ascend2Json(it))
                TourbookExportFormat.eJSONVERBOSE -> jsonAscends.put(
                    JSONObject(TourbookEntryVerbose(it, _db).asMap())
                )
                else -> {}
            }
        }
        writeStrToUri(uri, jsonAscends.toString(JSON_INDENT_SPACE))
    }

    @Throws(JSONException::class)
    private fun _ascend2Json(ascend: Ascend): JSONObject {
        val jsonAscend = JSONObject().apply {
            put(TourbookExchangeKeys.eROUTE.key, ascend.routeId.toString())
            put(TourbookExchangeKeys.eSTYLE.key, ascend.styleId.toString())
            put(TourbookExchangeKeys.eYEAR.key, ascend.year.toString())
            put(TourbookExchangeKeys.eMONTH.key, ascend.month.toString())
            put(TourbookExchangeKeys.eDAY.key, ascend.day.toString())
        }
        val partnerList = JSONArray()
        _db.getPartnerNames(ascend.partnerIds.orEmpty()).map { partnerList.put(it) }
        jsonAscend.put(TourbookExchangeKeys.ePARTNERS.key, partnerList)
        jsonAscend.put(TourbookExchangeKeys.eNOTES.key, ascend.notes)

        return jsonAscend
    }
}
