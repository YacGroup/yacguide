package com.example.paetz.yacguide.network

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
