/*
 * Copyright (C) 2019, 2022 Axel Paetzold
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

import com.yacgroup.yacguide.UpdateListener
import com.yacgroup.yacguide.utils.ParserUtils

import org.json.JSONException

import java.util.LinkedList
import kotlin.jvm.Throws

enum class ExitCode {
    SUCCESS,
    ERROR,
    ABORT
}

abstract class JSONWebParser : NetworkListener {
    var listener: UpdateListener? = null
    protected val baseUrl = "http://db-sandsteinklettern.gipfelbuch.de/"
    protected var networkRequests: LinkedList<NetworkRequest> = LinkedList()
    private var _exitCode: ExitCode = ExitCode.SUCCESS
    private var _processedRequestsCount: Int = 0

    override fun onNetworkTaskResolved(requestId: NetworkRequestUId, result: String) {
        if (_exitCode == ExitCode.SUCCESS) {
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
                _exitCode = ExitCode.ERROR
            } catch (e: IllegalArgumentException) {
            }
        }
        if (++_processedRequestsCount == networkRequests.size) {
            onFinalTaskResolved(_exitCode)
            _exitCode = ExitCode.SUCCESS
        }
    }

    fun fetchData() {
        _processedRequestsCount = 0
        initNetworkRequests()
        networkRequests.forEach {
            NetworkTask(it.requestId, this).execute(it.url)
        }
    }

    fun abort() {
        _exitCode = ExitCode.ABORT
    }

    @Throws(JSONException::class)
    protected abstract fun parseData(requestId: NetworkRequestUId, json: String)

    protected abstract fun initNetworkRequests()

    // NetworkListener
    protected open fun onFinalTaskResolved(exitCode: ExitCode) {
        listener?.onUpdateFinished(exitCode)
    }
}
