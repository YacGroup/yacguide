/*
 * Copyright (C) 2018 Axel Pätzold
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

import android.os.AsyncTask

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class NetworkTask(private val _requestId: NetworkRequestType,
                  private val _listener: NetworkListener) : AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg urls: String): String? {
        if (urls.size != 1) {
            System.err.println("Can only process one url")
            return null
        }

        val connection = URL(urls[0]).openConnection() as HttpURLConnection

        return try {
            connection.inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            System.err.println(e)
            ""
        } finally {
            connection.disconnect()
        }
    }

    override fun onPostExecute(result: String) {
        _listener.onNetworkTaskResolved(_requestId, result)
    }
}
