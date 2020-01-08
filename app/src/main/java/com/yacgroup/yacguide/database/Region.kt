package com.example.paetz.yacguide.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class Region {

    @PrimaryKey
    var id: Int = 0

    var name: String? = null
    var country: String? = null
}
