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

import com.yacgroup.yacguide.utils.ParserUtils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.ArrayList

class TourbookExporter(private val _db: AppDatabase) {

    private val _routeIdKey = "routeId"
    private val _styleIdKey = "styleId"
    private val _yearKey = "year"
    private val _monthKey = "month"
    private val _dayKey = "day"
    private val _partnersKey = "partners"
    private val _notesKey = "notes"

    @Throws(JSONException::class)
    fun exportTourbook(filePath: String) {
        val ascends = _db.ascendDao().all
        val jsonAscends = JSONArray()
        for (ascend in ascends) {
            jsonAscends.put(ascend2Json(ascend))
        }
        val file = File(filePath)
        try {
            val outStream = FileOutputStream(file)
            outStream.write(jsonAscends.toString().toByteArray(StandardCharsets.UTF_8))
            outStream.close()
        } catch (e: Exception) {
            throw JSONException("")
        }
    }

    @Throws(JSONException::class)
    fun importTourbook(filePath: String) {
        val file = File(filePath)
        val jsonString: String
        try {
            jsonString = FileInputStream(file).bufferedReader().readText()
        } catch (e: Exception) {
            throw JSONException("")
        }
        writeJsonStringToDatabase(jsonString)
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
        for (id in ascend.partnerIds.orEmpty()) {
            val partner = _db.partnerDao().getPartner(id)

            partner?.name?.let {
                partnerList.put(it)
            }
        }
        jsonAscend.put(_partnersKey, partnerList)
        jsonAscend.put(_notesKey, ascend.notes)

        return jsonAscend
    }

    @Throws(JSONException::class)
    private fun writeJsonStringToDatabase(jsonString: String) {
        val jsonAscends = JSONArray(jsonString)
        _db.deleteAscends()
        _db.partnerDao().deleteAll()
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

                var partner = _db.partnerDao().all.find { name == it.name }

                if (partner == null) {
                    partner = Partner()
                    partner.id = partnerId++
                    partner.name = name
                    _db.partnerDao().insert(partner)
                }
                partnerIds.add(partner.id)
            }

            ascend.partnerIds = partnerIds
            _db.ascendDao().insert(ascend)
            _db.checkBitMasks(ascend)
        }
    }
}
