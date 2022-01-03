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

import kotlin.reflect.full.memberProperties

class TourbookEntryVerbose(ascend: Ascend, db: DatabaseWrapper) {
    companion object {
        /*
         * Return a list of all property names of this class
         * which are treated as fields to be exported.
         */
        fun keys(): List<String> = TourbookEntryVerbose::class.memberProperties.map{ it.name }
    }

    var country: String
    var regionName: String
    var sectorFirstName: String
    var sectorSecondName: String
    var rockFirstName: String
    var rockSecondName: String
    var routeFirstName: String
    var routeSecondName: String
    var notes: String
    var date: String
    var style: String
    var partners: String

    init {
        val route = db.getRoute(ascend.routeId)
        val rock = route?.parentId?.let { db.getRock(it) }
        val sector = rock?.parentId?.let { db.getSector(it) }
        val region = sector?.parentId?.let { db.getRegion(it) }

        country = region?.country.orEmpty()
        regionName = region?.name.orEmpty()
        sectorFirstName = ParserUtils.decodeObjectNames(sector?.name).first
        sectorSecondName = ParserUtils.decodeObjectNames(sector?.name).second
        rockFirstName = ParserUtils.decodeObjectNames(rock?.name).first
        rockSecondName = ParserUtils.decodeObjectNames(rock?.name).second
        routeFirstName = ParserUtils.decodeObjectNames(route?.name).first
        routeSecondName = ParserUtils.decodeObjectNames(route?.name).second
        notes = ascend.notes.orEmpty()
        date = "%02d.%02d.%4d".format(ascend.day, ascend.month, ascend.year)
        style = AscendStyle.fromId(ascend.styleId)?.styleName.orEmpty()
        partners = db.getPartnerNames(ascend.partnerIds.orEmpty()).joinToString(",")
    }

    fun asMap(): Map<String, String> {
        val self = this
        return buildMap {
            TourbookEntryVerbose::class.memberProperties.forEach {
                if (it.name in keys()) {
                    put(it.name, it.get(self).toString())
                }
            }
        }
    }

    fun values(): List<String> = keys().map { asMap()[it]!! }
}
