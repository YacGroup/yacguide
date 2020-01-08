package com.example.paetz.yacguide.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class Sector {

    @PrimaryKey
    var id: Int = 0

    var nr: Float = 0f
    var name: String? = null
    var parentId: Int = 0
}
