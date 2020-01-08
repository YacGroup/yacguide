package com.example.paetz.yacguide.database.Comment

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.util.HashMap

@Entity
class RegionComment {

    @PrimaryKey
    var id: Int = 0

    var qualityId: Int = 0
    var text: String? = null
    var regionId: Int = 0

    companion object {

        // This needs to be in sync with sandsteinklettern.de!
        val QUALITY_MAP: Map<Int, String> = object : HashMap<Int, String>() {
            init {
                put(1, "international bedeutend")
                put(2, "national bedeutend")
                put(3, "lohnt einen Abstecher")
                put(4, "lokal bedeutend")
                put(5, "unbedeutend")
            }
        }
    }
}
