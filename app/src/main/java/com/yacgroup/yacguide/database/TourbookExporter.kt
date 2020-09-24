/*
 * Copyright (C) 2019 Fabian Kantereit
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

package com.yacgroup.yacguide.database

import android.content.ContentResolver
import android.net.Uri

import com.yacgroup.yacguide.utils.ParserUtils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.*
import java.util.ArrayList

class TourbookExporter(
        private val _db: DatabaseWrapper,
        private val _contentResolver: ContentResolver) {

    private val _routeIdKey = "routeId"
    private val _styleIdKey = "styleId"
    private val _yearKey = "year"
    private val _monthKey = "month"
    private val _dayKey = "day"
    private val _partnersKey = "partners"
    private val _notesKey = "notes"

    fun exportTourbook(uri: Uri) {
        val jsonAscends = JSONArray()
        _db.getAscends().map { jsonAscends.put(ascend2Json(it)) }
        _writeStrToUri(uri, jsonAscends.toString())
    }

    @Throws(IOException::class)
    private fun _writeStrToUri(uri: Uri, str: String) {
        try {
            _contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(str.toByteArray(Charsets.UTF_8))
                }
            }
        } catch (e: IOException) {
            throw IOException("Write to URI '${uri}' failed.")
        }
    }

    @Throws(JSONException::class)
    fun importTourbook(uri: Uri) {
        val jsonString = _readTextFromUri(uri)
        writeJsonStringToDatabase(jsonString)
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

    @Throws(JSONException::class)
    private fun ascend2Json(ascend: Ascend): JSONObject {
        val jsonAscend = JSONObject()
        jsonAscend.put(_routeIdKey, ascend.routeId.toString())
        jsonAscend.put(_styleIdKey, ascend.styleId.toString())
        jsonAscend.put(_yearKey, ascend.year.toString())
        jsonAscend.put(_monthKey, ascend.month.toString())
        jsonAscend.put(_dayKey, ascend.day.toString())
        val partnerList = JSONArray()
        _db.getPartnerNames(ascend.partnerIds.orEmpty()).map { partnerList.put(it) }
        jsonAscend.put(_partnersKey, partnerList)
        jsonAscend.put(_notesKey, ascend.notes)

        return jsonAscend
    }

    @Throws(JSONException::class)
    fun writeJsonStringToDatabase(jsonString: String) {
        val jsonAscends = JSONArray(jsonString)
        _db.deleteAscends()
        _db.deletePartners()
        val ascends = mutableListOf<Ascend>()
        val partners = mutableListOf<Partner>()
        var ascendId = 1
        var partnerId = 1
        for (i in 0 until jsonAscends.length()) {
            val jsonAscend = jsonAscends.getJSONObject(i)
            val routeId = ParserUtils.jsonField2Int(jsonAscend, _routeIdKey)
            val ascend = Ascend()
            ascend.id = ascendId++
            ascend.routeId = routeId
            ascend.styleId = ParserUtils.jsonField2Int(jsonAscend, _styleIdKey)
            ascend.year = ParserUtils.jsonField2Int(jsonAscend, _yearKey)
            ascend.month = ParserUtils.jsonField2Int(jsonAscend, _monthKey)
            ascend.day = ParserUtils.jsonField2Int(jsonAscend, _dayKey)
            ascend.notes = jsonAscend.getString(_notesKey)
            val partnerNames = jsonAscend.getJSONArray(_partnersKey)
            val partnerIds = ArrayList<Int>()
            for (j in 0 until partnerNames.length()) {
                val name = partnerNames.getString(j).trim { it <= ' ' }

                var partner = partners.find { name == it.name }

                if (partner == null) {
                    partner = Partner()
                    partner.id = partnerId++
                    partner.name = name
                    partners.add(partner)
                }
                partnerIds.add(partner.id)
            }

            ascend.partnerIds = partnerIds
            ascends.add(ascend)
        }
        _db.addPartners(partners)
        _db.addAscends(ascends)
    }
}
