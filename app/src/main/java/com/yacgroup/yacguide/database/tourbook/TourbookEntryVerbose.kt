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
import com.yacgroup.yacguide.database.AscendVerbose
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.ParserUtils

/*
 * Has two constructors:
 * - (Ascend, DatabaseWrapper) resolves the route/rock/sector/region hierarchy and
 *   partner names for a single ascend via separate DB lookups. Simple, but causes
 *   an N+1 query pattern when used for every ascend of a tourbook export.
 * - (AscendVerbose, partnerNames) builds the same fields from a single query joining
 *   the whole hierarchy (see AscendDao.getAscendsVerbose()) plus a pre-fetched partner
 *   id -> name map, so exporting the whole tourbook only needs two DB queries in total.
 */
class TourbookEntryVerbose {
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

    constructor(ascend: Ascend, db: DatabaseWrapper) {
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

    constructor(ascend: AscendVerbose, partnerNames: Map<Int, String>) {
        country = ascend.country.orEmpty()
        regionName = ascend.regionName.orEmpty()
        ParserUtils.decodeObjectNames(ascend.sectorName).let {
            sectorFirstName = it.first
            sectorSecondName = it.second
        }
        ParserUtils.decodeObjectNames(ascend.rockName).let {
            rockFirstName = it.first
            rockSecondName = it.second
        }
        ParserUtils.decodeObjectNames(ascend.routeName).let {
            routeFirstName = it.first
            routeSecondName = it.second
        }
        routeGrade = ascend.routeGrade.orEmpty()
        notes = ascend.notes.orEmpty()
        date = "%02d.%02d.%4d".format(ascend.day, ascend.month, ascend.year)
        style = AscendStyle.fromId(ascend.styleId)?.styleName.orEmpty()
        partners = ascend.partnerIds.orEmpty().joinToString(",") {
            partnerNames[it] ?: DatabaseWrapper.UNKNOWN_NAME
        }
    }

    fun asMap(): Map<String, String> = keys().zip(values()).toMap()

    fun values(): List<String> = listOf(
        country, regionName, sectorFirstName, sectorSecondName,
        rockFirstName, rockSecondName, routeFirstName, routeSecondName,
        routeGrade, notes, date, style, partners
    )
}
