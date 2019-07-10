package com.example.paetz.yacguide.map.download

import android.content.Context
import com.example.paetz.yacguide.utils.FileDownloader
import com.example.paetz.yacguide.utils.unpackZip
import java.io.File

enum class DownloadState {
    STATE_NONE,
    STATE_DOWNLOADING,
    STATE_DOWNLOADED,
    STATE_EXTRACTING,
    STATE_FINISHED,
}

interface MapDownloadListener {
    fun onDownloadCompleted(map: MapFile)
}

class MapDownloadManager(
        val listener: MapDownloadListener
) {
    companion object {
        val availableMaps = listOf(
                MapFile("Weltkarte", "world.map", "", ""),
                MapFile("Sachsen", "sachsen.map", "", ""),
                MapFile("Tschechische Republik", "Czech_Republic.map", "","")
        )
        val availableStyles = listOf(
                StyleFile("Kartenstil", "Elevate.xml", "")
        )

        val state: MutableMap<MapFile, DownloadState> = HashMap()
    }

    fun downloadMap(context: Context, map: MapFile) {
        when (state[map]) {
            DownloadState.STATE_NONE -> {
                state[map] = DownloadState.STATE_DOWNLOADING
                FileDownloader(map.archiveFileName, map.url, context).download {
                    state[map] = DownloadState.STATE_DOWNLOADED
                    downloadMap(context, map)
                }
            }
            DownloadState.STATE_DOWNLOADING -> {}
            DownloadState.STATE_DOWNLOADED -> {
                state[map] = DownloadState.STATE_EXTRACTING
                if (map.mapFileName != map.archiveFileName)
                    unpackZip(context.getExternalFilesDir(null)!!.path + File.separator, map.archiveFileName)
                state[map] = DownloadState.STATE_FINISHED
                downloadMap(context, map)
            }
            DownloadState.STATE_EXTRACTING -> {}
            DownloadState.STATE_FINISHED -> {
                listener.onDownloadCompleted(map)
            }
        }
    }

    fun getDownloadState(map: MapFile): DownloadState? {
        return state[map]
    }
}
