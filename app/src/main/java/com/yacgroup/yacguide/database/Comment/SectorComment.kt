package com.yacgroup.yacguide.database.Comment

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.util.HashMap

@Entity
class SectorComment {

    @PrimaryKey
    var id: Int = 0

    var qualityId: Int = 0
    var text: String? = null
    var sectorId: Int = 0

    companion object {

        // This needs to be in sync with sandsteinklettern.de!
        val QUALITY_MAP: Map<Int, String> = object : HashMap<Int, String>() {
            init {
                put(1, "Hauptteilgebiet")
                put(2, "lohnendes Gebiet")
                put(3, "kann man mal hingehen")
                put(4, "weniger bedeutend")
                put(5, "unbedeutend")
            }
        }
    }
}
