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
import org.json.JSONObject

open class JsonExporter(
    contentResolver: ContentResolver,
    private val _db: DatabaseWrapper): BaseExporter(contentResolver, _db) {

    companion object {
        private const val _JSON_INDENT_SPACE = 2
    }

    override fun export(uri: Uri) {
        val jsonAscends = JSONArray().apply {
            _db.getAscends().forEach { put(ascend2Json(it)) }
        }
        writeStrToUri(uri, jsonAscends.toString(_JSON_INDENT_SPACE))
    }

    protected open fun ascend2Json(ascend: Ascend): JSONObject {
        val partnerList = JSONArray().apply {
            _db.getPartnerNames(ascend.partnerIds.orEmpty()).forEach { put(it) }
        }
        return JSONObject().apply {
            put(TourbookExchangeKeys.eROUTE.key, ascend.routeId.toString())
            put(TourbookExchangeKeys.eSTYLE.key, ascend.styleId.toString())
            put(TourbookExchangeKeys.eYEAR.key, ascend.year.toString())
            put(TourbookExchangeKeys.eMONTH.key, ascend.month.toString())
            put(TourbookExchangeKeys.eDAY.key, ascend.day.toString())
            put(TourbookExchangeKeys.ePARTNERS.key, partnerList)
            put(TourbookExchangeKeys.eNOTES.key, ascend.notes)
        }
    }
}
