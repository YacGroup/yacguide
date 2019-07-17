package com.example.paetz.yacguide.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.util.HashMap

@Entity
class Route {

    @PrimaryKey
    var id: Int = 0

    var nr: Float = 0.toFloat()
    var statusId: Int = 0
    var name: String? = null
    var grade: String? = null
    var firstAscendLeader: String = ""
    var firstAscendFollower: String? = null
    var firstAscendDate: String? = null
    var typeOfClimbing: String? = null
    var description: String? = null
    var ascendCount: Int = 0
    var parentId: Int = 0

    companion object {

        // This needs to be in sync with sandsteinklettern.de!
        val STATUS: Map<Int, String> = object : HashMap<Int, String>() {
            init {
                put(1, "anerkannt.")
                put(2, "zeitlich gesperrt.")
                put(3, "voll gesperrt.")
                put(4, "eine Erstbegehung.")
                put(5, "nicht anerkannt.")
                put(6, "nur eine Erwähnung.")
                put(7, "ein Projekt.")
            }
        }
    }
}
