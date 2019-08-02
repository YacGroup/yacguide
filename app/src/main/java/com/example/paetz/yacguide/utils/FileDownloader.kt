package com.example.paetz.yacguide.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import java.io.File

class FileDownloader(
        val name: String,
        val url: String,
        val context: Context
) {
    private val _manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private fun queryDownloadState(id: Long): Boolean {
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = _manager.query(query)
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                return true
            }
        }
        return false
    }

    fun download(onCompletion: (name: String) -> Unit) {
        val file = File(context.getExternalFilesDir(null)?.path + File.separator + name)
        if (!file.exists()) {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setDescription("Downloading required map files")
            request.setTitle("yacguide map files")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalFilesDir(context, null, name)
            val id = _manager.enqueue(request)

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val action = intent?.action
                    if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                        if (queryDownloadState(id)) {
                            onCompletion(name)
                        }
                    }
                }
            }

            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        } else {
            onCompletion(name)
        }
    }
}
