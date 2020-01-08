package com.example.paetz.yacguide.database

import android.arch.persistence.room.Entity

@Entity(primaryKeys = ["name"])
class Country {
    var name = ""
}
