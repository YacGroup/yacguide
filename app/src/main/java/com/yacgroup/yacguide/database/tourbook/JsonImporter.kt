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
import com.yacgroup.yacguide.database.Partner
import com.yacgroup.yacguide.utils.ParserUtils
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class JsonImporter(
    private val _db: DatabaseWrapper,
    private val _contentResolver: ContentResolver) {

    fun import(uri: Uri) {
        _writeJsonStringToDatabase(_readTextFromUri(uri))
    }

    @Throws(JSONException::class)
    private fun _writeJsonStringToDatabase(jsonString: String) {
        val jsonAscends = JSONArray(jsonString)
        val ascents = mutableListOf<Ascend>()
        val partners = mutableListOf<Partner>()
        var ascentId = 1
        var partnerId = 1
        for (i in 0 until jsonAscends.length()) {
            val jsonAscend = jsonAscends.getJSONObject(i)
            val partnerNames = jsonAscend.getJSONArray(TourbookExchangeKeys.ePARTNERS.key)
            val partnerIds = ArrayList<Int>()
            for (j in 0 until partnerNames.length()) {
                val name = partnerNames.getString(j).trim { it <= ' ' }
                var partner = partners.find { name == it.name }
                if (partner == null) {
                    partner = Partner(
                        id = partnerId++,
                        name = name
                    )
                    partners.add(partner)
                }
                partnerIds.add(partner.id)
            }
            val ascent = Ascend(
                id = ascentId++,
                routeId = ParserUtils.jsonField2Int(jsonAscend, TourbookExchangeKeys.eROUTE.key),
                styleId = ParserUtils.jsonField2Int(jsonAscend, TourbookExchangeKeys.eSTYLE.key),
                year = ParserUtils.jsonField2Int(jsonAscend, TourbookExchangeKeys.eYEAR.key),
                month = ParserUtils.jsonField2Int(jsonAscend, TourbookExchangeKeys.eMONTH.key),
                day = ParserUtils.jsonField2Int(jsonAscend, TourbookExchangeKeys.eDAY.key),
                partnerIds = partnerIds,
                notes = jsonAscend.getString(TourbookExchangeKeys.eNOTES.key)
            )
            _db.deleteAscends()
            _db.deletePartners()
            ascents.add(ascent)
        }
        _db.addPartners(partners)
        _db.addAscends(ascents)
    }

    @Throws(IOException::class)
    private fun _readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        _contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }
}
