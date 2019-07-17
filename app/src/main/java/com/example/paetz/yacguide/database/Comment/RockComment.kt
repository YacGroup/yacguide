package com.example.paetz.yacguide.database.Comment

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.util.HashMap

@Entity
class RockComment {

    @PrimaryKey
    var id: Int = 0

    var qualityId: Int = 0
    var text: String? = null
    var rockId: Int = 0

    companion object {

        // This needs to be in sync with sandsteinklettern.de!
        val QUALITY_MAP: Map<Int, String> = object : HashMap<Int, String>() {
            init {
                put(1, "Hauptgipfel")
                put(2, "lohnender Gipfel")
                put(3, "Durchschnittsgipfel")
                put(4, "Quacke")
                put(5, "Dreckhaufen")
            }
        }
    }
}
