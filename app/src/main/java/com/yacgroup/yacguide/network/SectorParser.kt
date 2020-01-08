package com.yacgroup.yacguide.network

import com.yacgroup.yacguide.UpdateListener
import com.yacgroup.yacguide.database.AppDatabase
import com.yacgroup.yacguide.database.Comment.RegionComment
import com.yacgroup.yacguide.database.Sector
import com.yacgroup.yacguide.utils.ParserUtils

import org.json.JSONArray
import org.json.JSONException

class SectorParser(db: AppDatabase,
                   listener: UpdateListener,
                   private val _regionId: Int) : JSONWebParser(db, listener) {

    init {
        networkRequests.add(NetworkRequest(
                NetworkRequestType.DATA_REQUEST_ID,
                "${baseUrl}jsonteilgebiet.php?app=yacguide&gebietid=$_regionId"
        ))
        networkRequests.add(NetworkRequest(
                NetworkRequestType.COMMENTS_REQUEST_ID,
                "${baseUrl}jsonkomment.php?app=yacguide&gebietid=$_regionId"
        ))
    }

    @Throws(JSONException::class)
    override fun parseData(requestId: NetworkRequestType, json: String) {
        if (requestId === NetworkRequestType.DATA_REQUEST_ID) {
            parseSectors(json)
        } else {
            parseRegionComments(json)
        }
    }

    @Throws(JSONException::class)
    private fun parseSectors(json: String) {
        db.sectorDao().deleteAll(_regionId)
        val jsonSectors = JSONArray(json)
        for (i in 0 until jsonSectors.length()) {
            val jsonSector = jsonSectors.getJSONObject(i)
            val s = Sector()
            s.id = ParserUtils.jsonField2Int(jsonSector, "sektor_ID")
            s.name = ParserUtils.jsonField2String(jsonSector, "sektorname_d", "sektorname_cz")
            s.nr = ParserUtils.jsonField2Float(jsonSector, "sektornr")
            s.parentId = _regionId
            db.sectorDao().insert(s)
        }
    }

    @Throws(JSONException::class)
    private fun parseRegionComments(json: String) {
        db.regionCommentDao().deleteAll(_regionId)
        val jsonComments = JSONArray(json)
        for (i in 0 until jsonComments.length()) {
            val jsonComment = jsonComments.getJSONObject(i)
            val comment = RegionComment()
            comment.id = ParserUtils.jsonField2Int(jsonComment, "komment_ID")
            comment.qualityId = ParserUtils.jsonField2Int(jsonComment, "qual")
            comment.text = jsonComment.getString("kommentar")
            comment.regionId = _regionId
            db.regionCommentDao().insert(comment)
        }
    }
}
