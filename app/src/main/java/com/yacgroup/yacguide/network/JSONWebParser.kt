/*
 * Copyright (C) 2019, 2022, 2023 Axel Paetzold
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
import com.yacgroup.yacguide.utils.DataUId

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
    private var _dataUId: DataUId = DataUId(0, "")
    private var _exitCode: ExitCode = ExitCode.SUCCESS
    private var _processedRequestsCount: Int = 0

    override fun onNetworkTaskResolved(request: NetworkRequest, result: String) {
        when (_exitCode) {
            ExitCode.ABORT -> return
            ExitCode.ERROR -> _exitOnFinalTask()
            ExitCode.SUCCESS -> {
                try {
                    if (result.equals("null", ignoreCase = true)) {
                        // sandsteinklettern.de returns "null" string if there are no elements
                        throw IllegalArgumentException("")
                    }
                    // remove HTML-encoded characters
                    parseData(
                        request,
                        ParserUtils.resolveToUtf8(result)
                    )
                } catch (e: JSONException) {
                    _exitCode = ExitCode.ERROR
                    listener?.onUpdateError(_dataUId.name)
                } catch (e: IllegalArgumentException) {
                }
                _exitOnFinalTask()
            }
        }
    }

    fun fetchData(dataUId: DataUId) {
        _dataUId = dataUId
        _exitCode = ExitCode.SUCCESS
        _processedRequestsCount = 0
        initNetworkRequests(dataUId)
        networkRequests.forEach {
            NetworkTask(it, this).execute()
        }
    }

    fun abort() {
        _exitCode = ExitCode.ABORT
        onFinalTaskResolved(_exitCode)
    }

    @Throws(JSONException::class)
    protected abstract fun parseData(request: NetworkRequest, json: String)

    protected abstract fun initNetworkRequests(dataUId: DataUId)

    protected open fun onFinalTaskResolved(exitCode: ExitCode) {
        listener?.onUpdateFinished(exitCode)
    }

    private fun _exitOnFinalTask() {
        if (++_processedRequestsCount == networkRequests.size) {
            onFinalTaskResolved(_exitCode)
        }
    }
}
