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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class NetworkTask(private val _request: NetworkRequest,
                  private val _scope: CoroutineScope,
                  private val _listener: NetworkListener) {

    fun execute() = _scope.launch {
        val result = _performNetworkRequest()
        _listener.onNetworkTaskResolved(NetworkAnswer(_request, result))
    }

    private suspend fun _performNetworkRequest(): String = withContext(Dispatchers.IO) {
        val connection = URL(_request.url).openConnection() as HttpURLConnection

        return@withContext try {
            connection.inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            System.err.println(e)
            ""
        } finally {
            connection.disconnect()
        }
    }
}
