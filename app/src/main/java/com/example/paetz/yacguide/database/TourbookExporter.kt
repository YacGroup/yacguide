package com.example.paetz.yacguide.database

import com.example.paetz.yacguide.utils.ParserUtils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.ArrayList

class TourbookExporter(private val db: AppDatabase) {

    private val _routeIdKey = "routeId"
    private val _styleIdKey = "styleId"
    private val _yearkey = "year"
    private val _monthkey = "month"
    private val _dayKey = "day"
    private val _partnersKey = "partners"
    private val _notesKey = "notes"

    @Throws(JSONException::class)
    fun exportTourbook(filePath: String) {
        val ascends = db.ascendDao().all
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
        jsonAscend.put(_yearkey, ascend.year.toString())
        jsonAscend.put(_monthkey, ascend.month.toString())
        jsonAscend.put(_dayKey, ascend.day.toString())
        val partnerList = JSONArray()
        for (id in ascend.partnerIds!!) {
            val partner = db.partnerDao().getPartner(id)
            if (partner != null) {
                partnerList.put(partner.name)
            }
        }
        jsonAscend.put(_partnersKey, partnerList)
        jsonAscend.put(_notesKey, ascend.notes)

        return jsonAscend
    }

    @Throws(JSONException::class)
    private fun writeJsonStringToDatabase(jsonString: String) {
        val jsonAscends = JSONArray(jsonString)
        for (ascend in db.ascendDao().all) {
            db.deleteAscend(ascend)
        }
        db.partnerDao().deleteAll()
        var ascendId = 1
        var partnerId = 1
        for (i in 0 until jsonAscends.length()) {
            val jsonAscend = jsonAscends.getJSONObject(i)
            val routeId = ParserUtils.jsonField2Int(jsonAscend, _routeIdKey)
            val ascend = Ascend()
            ascend.id = ascendId++
            ascend.routeId = routeId
            ascend.styleId = ParserUtils.jsonField2Int(jsonAscend, _styleIdKey)
            ascend.year = ParserUtils.jsonField2Int(jsonAscend, _yearkey)
            ascend.month = ParserUtils.jsonField2Int(jsonAscend, _monthkey)
            ascend.day = ParserUtils.jsonField2Int(jsonAscend, _dayKey)
            ascend.notes = jsonAscend.getString(_notesKey)
            val partnerNames = jsonAscend.getJSONArray(_partnersKey)
            val partnerIds = ArrayList<Int>()
            for (j in 0 until partnerNames.length()) {
                val name = partnerNames.getString(j).trim { it <= ' ' }
                var partnerAvailable = false
                for (partner in db.partnerDao().all) {
                    if (partner.name == name) {
                        partnerIds.add(partner.id)
                        partnerAvailable = true
                        break

                    }
                }
                if (!partnerAvailable) {
                    val newPartner = Partner()
                    newPartner.id = partnerId
                    newPartner.name = name
                    db.partnerDao().insert(newPartner)
                    partnerIds.add(partnerId++)
                }
            }
            ascend.partnerIds = partnerIds
            db.ascendDao().insert(ascend)
            val route = db.routeDao().getRoute(routeId)
            if (route != null) {
                db.routeDao().updateAscendCount(db.routeDao().getAscendCount(routeId) + 1, routeId)
                db.rockDao().updateAscended(true, route.parentId)
            }
        }
    }
}
