package com.example.paetz.yacguide.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.util.ArrayList

@Entity
class Ascend {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var routeId: Int = 0
    var styleId: Int = 0 // id for os, RP, af, ...
    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    var partnerIds: ArrayList<Int>? = null
    var notes: String? = null

}
