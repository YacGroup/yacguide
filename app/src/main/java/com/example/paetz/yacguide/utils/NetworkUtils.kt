package com.example.paetz.yacguide.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import java.net.URLEncoder

import java.util.HashMap

object NetworkUtils {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo?.isConnected ?: false
    }

    fun encodeString2Url(str: String): String {
        return URLEncoder.encode(str, "UTF-8")
    }
}
