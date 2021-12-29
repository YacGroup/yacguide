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

class TourbookEntryVerbose(_ascend: Ascend, val _db: DatabaseWrapper) {
    companion object {
        /*
         * Return a list of all property names of this class
         * which are treated as fields to be exported.
         */
        fun keys(): List<String> {
            return buildList {
                TourbookEntryVerbose::class.memberProperties.forEach{
                    if (!it.name.startsWith("_")) {
                        add(it.name)
                    }
                }
            }
        }
    }

    var country: String
    var regionName: String
    var sectorName: String
    var rockName: String
    var routeName: String
    var notes: String
    var date: String
    var style: String
    var partners: String

    init {
        val route = _db.getRoute(_ascend.routeId)
        val rock = route?.parentId?.let { it -> _db.getRock(it) }
        val sector = rock?.parentId?.let { it -> _db.getSector(it) }
        val region = sector?.parentId?.let { it -> _db.getRegion(it) }

        country = region?.country.orEmpty()
        regionName = region?.name.orEmpty()
        sectorName = ParserUtils.decodeObjectNames(sector?.name).first
        rockName = ParserUtils.decodeObjectNames(rock?.name).first
        routeName = ParserUtils.decodeObjectNames(route?.name).first
        notes = _ascend.notes.orEmpty()
        date = "%02d.%02d.%4d".format(_ascend.day, _ascend.month, _ascend.year)
        style = AscendStyle.fromId(_ascend.styleId)?.styleName.orEmpty()
        partners = _db.getPartnerNames(_ascend.partnerIds.orEmpty()).joinToString(",")
    }

    /*
     * Return tour book entry as map
     */
    fun asMap(): Map<String, String> {
        val thisObj = this
        return buildMap {
            TourbookEntryVerbose::class.memberProperties.forEach {
                if (it.name in keys()) {
                    put(it.name, it.get(thisObj).toString())
                }
            }
        }
    }

    /*
     * Return tour book values in the order of the keys
     */
    fun values(): List<String> {
        return buildList {
            keys().forEach{
                asMap()[it]?.let { it1 -> add(it1) }
            }
        }
    }
}
