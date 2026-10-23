/*
 * Copyright (C) 2021, 2026 Christian Sommer
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

    private val _country: String
    private val _regionName: String
    private val _sectorFirstName: String
    private val _sectorSecondName: String
    private val _rockFirstName: String
    private val _rockSecondName: String
    private val _routeFirstName: String
    private val _routeSecondName: String
    private val _routeGrade: String
    private val _notes: String
    private val _date: String
    private val _style: String
    private val _partners: String

    init {
        val route = db.getRoute(ascend.routeId)
        val rock = route?.parentId?.let { db.getRock(it) }
        val sector = rock?.parentId?.let { db.getSector(it) }
        val region = sector?.parentId?.let { db.getRegion(it) }

        _country = region?.country.orEmpty()
        _regionName = region?.name.orEmpty()
        ParserUtils.decodeObjectNames(sector?.name).let {
            _sectorFirstName = it.first
            _sectorSecondName = it.second
        }
        ParserUtils.decodeObjectNames(rock?.name).let {
            _rockFirstName = it.first
            _rockSecondName = it.second
        }
        ParserUtils.decodeObjectNames(route?.name).let {
            _routeFirstName = it.first
            _routeSecondName = it.second
        }
        _routeGrade = route?.grade.orEmpty()
        _notes = ascend.notes.orEmpty()
        _date = "%02d.%02d.%4d".format(ascend.day, ascend.month, ascend.year)
        _style = AscendStyle.fromId(ascend.styleId)?.styleName.orEmpty()
        _partners = db.getPartnerNames(ascend.partnerIds.orEmpty()).joinToString(",")
    }

    fun asMap(): Map<String, String> = keys().zip(values()).toMap()

    fun values(): List<String> = listOf(
        _country, _regionName, _sectorFirstName, _sectorSecondName,
        _rockFirstName, _rockSecondName, _routeFirstName, _routeSecondName,
        _routeGrade, _notes, _date, _style, _partners
    )
}
