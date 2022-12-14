/*
 * Copyright (C) 2019, 2022 Axel Paetzold
 * Copyright (C) 2021 Christian Sommer
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
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.*
import java.util.ArrayList

class TourbookExporter(
    private val _db: DatabaseWrapper,
    private val _contentResolver: ContentResolver) {

    private val JSON_INDENT_SPACE = 2

    private val _routeIdKey = "routeId"
    private val _styleIdKey = "styleId"
    private val _yearKey = "year"
    private val _monthKey = "month"
    private val _dayKey = "day"
    private val _partnersKey = "partners"
    private val _notesKey = "notes"

    var exportFormat: TourbookExportFormat = TourbookExportFormat.eJSON

    @Throws(IOException::class)
    fun exportTourbook(uri: Uri) {
        when (exportFormat) {
            TourbookExportFormat.eJSON, TourbookExportFormat.eJSONVERBOSE -> _exportAsJson(uri)
            TourbookExportFormat.eCSV -> _exportAsCsv(uri)
        }
    }

    /*
     * See https://commons.apache.org/proper/commons-csv/apidocs/index.html
     */
    @Throws(IOException::class)
    private fun _exportAsCsv(uri: Uri) {
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
            _writeStrToUri(uri, writer.toString())
        }
    }

    @Throws(IOException::class)
    private fun _exportAsJson(uri: Uri) {
        val jsonAscends = JSONArray()
        _db.getAscends().forEach {
            when (exportFormat) {
                TourbookExportFormat.eJSON -> jsonAscends.put(ascend2Json(it))
                TourbookExportFormat.eJSONVERBOSE -> jsonAscends.put(
                    JSONObject(TourbookEntryVerbose(it, _db).asMap())
                )
                else -> {}
            }
        }
        _writeStrToUri(uri, jsonAscends.toString(JSON_INDENT_SPACE))
    }

    @Throws(IOException::class)
    private fun _writeStrToUri(uri: Uri, str: String) {
        _contentResolver.openFileDescriptor(uri, "w")?.use { fileDescriptor ->
            FileOutputStream(fileDescriptor.fileDescriptor).use {
                it.write(str.toByteArray(Charsets.UTF_8))
            }
        }
    }

    @Throws(JSONException::class, IOException::class)
    fun importTourbook(uri: Uri) {
        val jsonString = _readTextFromUri(uri)
        _writeJsonStringToDatabase(jsonString)
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
        val jsonAscend = JSONObject().apply {
            put(_routeIdKey, ascend.routeId.toString())
            put(_styleIdKey, ascend.styleId.toString())
            put(_yearKey, ascend.year.toString())
            put(_monthKey, ascend.month.toString())
            put(_dayKey, ascend.day.toString())
        }
        val partnerList = JSONArray()
        _db.getPartnerNames(ascend.partnerIds.orEmpty()).map { partnerList.put(it) }
        jsonAscend.put(_partnersKey, partnerList)
        jsonAscend.put(_notesKey, ascend.notes)

        return jsonAscend
    }

    @Throws(JSONException::class)
    private fun _writeJsonStringToDatabase(jsonString: String) {
        val jsonAscends = JSONArray(jsonString)
        _db.deleteAscends()
        _db.deletePartners()
        val ascents = mutableListOf<Ascend>()
        val partners = mutableListOf<Partner>()
        var ascentId = 1
        var partnerId = 1
        for (i in 0 until jsonAscends.length()) {
            val jsonAscend = jsonAscends.getJSONObject(i)
            val partnerNames = jsonAscend.getJSONArray(_partnersKey)
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
                routeId = ParserUtils.jsonField2Int(jsonAscend, _routeIdKey),
                styleId = ParserUtils.jsonField2Int(jsonAscend, _styleIdKey),
                year = ParserUtils.jsonField2Int(jsonAscend, _yearKey),
                month = ParserUtils.jsonField2Int(jsonAscend, _monthKey),
                day = ParserUtils.jsonField2Int(jsonAscend, _dayKey),
                partnerIds = partnerIds,
                notes = jsonAscend.getString(_notesKey)
            )
            ascents.add(ascent)
        }
        _db.addPartners(partners)
        _db.addAscends(ascents)
    }
}
