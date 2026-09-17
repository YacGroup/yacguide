/*
 * Copyright (C) 2021 Christian Sommer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.yacgroup.yacguide.database.tourbook

import com.yacgroup.yacguide.database.Ascend
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.ParserUtils

class TourbookEntryVerbose(ascend: Ascend, db: DatabaseWrapper) {
    companion object {
        /*
         * Field names to be exported, in the same order as values().
         *
         * NOTE: This used to be derived via Kotlin reflection (memberProperties), but R8
         * has no idea that reflection reads these fields by name: with minification enabled
         * it strips/renames them regardless, silently turning every exported row empty.
         * Keep this list in sync with the properties below and with values() by hand instead.
         */
        fun keys(): List<String> = listOf(
            "country", "regionName", "sectorFirstName", "sectorSecondName",
            "rockFirstName", "rockSecondName", "routeFirstName", "routeSecondName",
            "routeGrade", "notes", "date", "style", "partners"
        )
    }

    val country: String
    val regionName: String
    val sectorFirstName: String
    val sectorSecondName: String
    val rockFirstName: String
    val rockSecondName: String
    val routeFirstName: String
    val routeSecondName: String
    val routeGrade: String
    val notes: String
    val date: String
    val style: String
    val partners: String

    init {
        val route = db.getRoute(ascend.routeId)
        val rock = route?.parentId?.let { db.getRock(it) }
        val sector = rock?.parentId?.let { db.getSector(it) }
        val region = sector?.parentId?.let { db.getRegion(it) }

        country = region?.country.orEmpty()
        regionName = region?.name.orEmpty()
        ParserUtils.decodeObjectNames(sector?.name).let {
            sectorFirstName = it.first
            sectorSecondName = it.second
        }
        ParserUtils.decodeObjectNames(rock?.name).let {
            rockFirstName = it.first
            rockSecondName = it.second
        }
        ParserUtils.decodeObjectNames(route?.name).let {
            routeFirstName = it.first
            routeSecondName = it.second
        }
        routeGrade = route?.grade.orEmpty()
        notes = ascend.notes.orEmpty()
        date = "%02d.%02d.%4d".format(ascend.day, ascend.month, ascend.year)
        style = AscendStyle.fromId(ascend.styleId)?.styleName.orEmpty()
        partners = db.getPartnerNames(ascend.partnerIds.orEmpty()).joinToString(",")
    }

    fun asMap(): Map<String, String> = keys().zip(values()).toMap()

    fun values(): List<String> = listOf(
        country, regionName, sectorFirstName, sectorSecondName,
        rockFirstName, rockSecondName, routeFirstName, routeSecondName,
        routeGrade, notes, date, style, partners
    )
}
