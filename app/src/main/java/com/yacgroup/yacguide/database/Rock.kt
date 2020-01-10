package com.yacgroup.yacguide.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class Rock {

    @PrimaryKey
    var id: Int = 0

    var nr: Float = 0.toFloat()
    var type: Char = ' '   // 'G' = Rock, 'M' = Massif, 'H' = Cage, 'B' = Boulder, 'N' = unofficial
    var status: Char = ' ' // 'X' = prohibited, 'Z' = temporarily prohibited, 'T' = partly prohibited
    var name: String? = null
    var longitude: Float = 0f
    var latitude: Float = 0f
    var ascendCountLead: Short = 0
    var ascendCountFollow: Short = 0
    var ascendCountBotch: Short = 0
    var ascendCountProject: Short = 0
    var parentId: Int = 0

    companion object {

        // This needs to be in sync with sandsteinklettern.de!
        const val typeSummit = 'G'
        const val typeMassif = 'M'
        const val typeBoulder = 'B'
        const val typeStonePit = 'S'
        const val typeAlpine = 'A'
        const val typeCave = 'H'
        const val typeUnofficial = 'N'

        const val statusTemporarilyProhibited = 'Z'
        const val statusProhibited = 'X'
        const val statusPartlyProhibited = 'T'
    }
}
