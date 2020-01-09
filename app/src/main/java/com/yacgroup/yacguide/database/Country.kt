package com.yacgroup.yacguide.database

import android.arch.persistence.room.Entity

@Entity(primaryKeys = ["name"])
class Country {
    var name = ""
}
