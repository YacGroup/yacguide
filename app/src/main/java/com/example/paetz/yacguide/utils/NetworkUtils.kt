package com.example.paetz.yacguide.utils

import android.content.Context
import android.net.ConnectivityManager

import java.util.HashMap

object NetworkUtils {

    // I couldn't find a proper java library, so let's escape it on our own
    private val URL_ENCODE_MAP = object : HashMap<String, String>() {
        init {
            put(" ", "%20")
            put("Ä", "%C4")
            put("ä", "%E4")
            put("Ö", "%D6")
            put("ö", "%F6")
            put("Ü", "%DC")
            put("ü", "%FC")
            put("ß", "%DF")
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun encodeString2Url(str: String): String {
        var result = str
        for ((key, value) in URL_ENCODE_MAP) {
            result = result.replace(key, value)
        }
        return result
    }
}
