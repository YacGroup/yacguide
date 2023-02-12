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

import com.yacgroup.yacguide.ClimbingObjectUId
import com.yacgroup.yacguide.UpdateListener
import com.yacgroup.yacguide.utils.ParserUtils
import kotlinx.coroutines.*
import org.json.JSONException
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.jvm.Throws

enum class ExitCode {
    SUCCESS,
    ERROR,
    ABORT
}

abstract class JSONWebParser : NetworkListener {
    var listener: UpdateListener? = null
    protected val networkScope = CoroutineScope(Dispatchers.Default)
    protected val baseUrl = "http://db-sandsteinklettern.gipfelbuch.de/"
    protected var networkRequests: LinkedList<NetworkRequest> = LinkedList()
    private var _climbingObjectUId: ClimbingObjectUId = ClimbingObjectUId(0, "")
    private var _exitCode: ExitCode = ExitCode.SUCCESS
    private var _processedRequestsCount: Int = 0
    private val _networkAnswersQueue = ConcurrentLinkedQueue<NetworkAnswer>()

    override fun onNetworkTaskResolved(networkAnswer: NetworkAnswer) {
        _networkAnswersQueue.add(networkAnswer)
    }

    fun fetchData(climbingObjectUId: ClimbingObjectUId) {
        _climbingObjectUId = climbingObjectUId
        _exitCode = ExitCode.SUCCESS
        _processedRequestsCount = 0
        _networkAnswersQueue.clear()
        initNetworkRequests(climbingObjectUId)
        networkRequests.forEach {
            NetworkTask(it, networkScope, this).execute()
        }
        _processNetworkAnswers()
    }

    fun abort() {
        _exitCode = ExitCode.ABORT
        networkScope.coroutineContext.cancelChildren()
        onFinalTaskResolved(_exitCode)
    }

    @Throws(JSONException::class)
    protected abstract fun parseData(request: NetworkRequest, json: String)

    protected abstract fun initNetworkRequests(climbingObjectUId: ClimbingObjectUId)

    protected open fun onFinalTaskResolved(exitCode: ExitCode) {
        listener?.onUpdateFinished(exitCode)
    }

    private fun _processNetworkAnswers() = networkScope.launch {
        while (isActive && _processedRequestsCount < networkRequests.size) {
            _networkAnswersQueue.poll()?.let { networkAnswer ->
                if (_exitCode == ExitCode.SUCCESS) {
                    try {
                        if (networkAnswer.answer.equals("null", ignoreCase = true)) {
                            // sandsteinklettern.de returns "null" string if there are no elements
                            throw IllegalArgumentException("")
                        }
                        // remove HTML-encoded characters
                        parseData(
                            networkAnswer.request,
                            ParserUtils.resolveToUtf8(networkAnswer.answer)
                        )
                    } catch (e: JSONException) {
                        _exitCode = ExitCode.ERROR
                        listener?.onUpdateError(_climbingObjectUId.name)
                    } catch (e: IllegalArgumentException) {
                    }
                }
                ++_processedRequestsCount
            }
        }
        onFinalTaskResolved(_exitCode)
    }
}
