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

package com.yacgroup.yacguide.network

import android.util.Log
import com.yacgroup.yacguide.UpdateListener
import com.yacgroup.yacguide.database.AppDatabase
import com.yacgroup.yacguide.utils.ParserUtils

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
            Log.d("import","Starting data import")
            db.beginTransaction()
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
                db.setTransactionSuccessful()
            } catch (e: JSONException) {
                _success = false
            } catch (e: IllegalArgumentException) {
            } finally {
                db.endTransaction()
                Log.d("import","Finished data import")
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