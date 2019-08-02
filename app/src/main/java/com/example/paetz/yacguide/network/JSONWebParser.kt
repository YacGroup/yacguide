package com.example.paetz.yacguide.network

import com.example.paetz.yacguide.UpdateListener
import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.utils.ParserUtils

import org.json.JSONException

import java.util.LinkedList

abstract class JSONWebParser(protected var db: AppDatabase,
                             private var _listener: UpdateListener) : NetworkListener {
    protected val baseUrl = "http://db-sandsteinklettern.gipfelbuch.de/"
    protected var networkRequests: LinkedList<NetworkRequest> = LinkedList()
    private var _success: Boolean = true
    private var _processedRequestsCount: Int = 0

    override fun onNetworkTaskResolved(requestId: NetworkRequestType, result: String) {
        if (_success) {
            try {
                if (result.equals("null", ignoreCase = true)) {
                    // sandsteinklettern.de returns "null" string if there are no elements
                    throw IllegalArgumentException("")
                }
                // remove HTML-encoded characters
                parseData(
                        requestId,
                        ParserUtils.resolveToUtf8(result)
                )
            } catch (e: JSONException) {
                _success = false
            } catch (e: IllegalArgumentException) {
            }
        }
        if (++_processedRequestsCount == networkRequests.size) {
            _listener.onEvent(_success)
            _success = true
        }
    }

    fun fetchData() {
        _processedRequestsCount = 0
        for (request in networkRequests) {
            NetworkTask(request.requestId, this).execute(request.url)
        }
    }

    @Throws(JSONException::class)
    protected abstract fun parseData(requestId: NetworkRequestType, json: String)
}
