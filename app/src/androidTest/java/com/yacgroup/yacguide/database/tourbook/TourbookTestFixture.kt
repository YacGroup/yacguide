/*
 * Copyright (C) 2026 Christian Sommer
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

import android.content.Context
import com.yacgroup.yacguide.database.Ascend
import com.yacgroup.yacguide.database.Country
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Partner
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.database.Sector
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.ParserUtils

/*********************************************************************
 * Database structure used by all tourbook export test classes:
 * - Country1:
 *      - Region1
 *          - Sector1 (two-part name)
 *              - Rock1 (two-part name)
 *                  - Route1 (two-part name)
 *                      - Ascend1 (full hierarchy, two partners, notes)
 * - Ascend2 references a routeId with no matching route, exercising the
 *   case of a tour whose route (and therefore rock/sector/region) has
 *   since been deleted from the database.
 ********************************************************************/
class TourbookTestFixture {

    companion object {
        val REGION = Region(1, "Region1", "Country1")
        val SECTOR = Sector(1, 1f, ParserUtils.encodeObjectNames("Sector1First", "Sector1Second"), REGION.id)
        val ROCK = Rock(1, 1f, 'G', ' ', ParserUtils.encodeObjectNames("Rock1First", "Rock1Second"), 0f, 0f, 0, SECTOR.id)
        val ROUTE = Route(
            1, 1f, 0,
            ParserUtils.encodeObjectNames("Route1First", "Route1Second"),
            "7a", "Leader1", "Follower1", "1990", "free", "desc", 0, ROCK.id
        )
        val PARTNER_1 = Partner(1, "Partner1")
        val PARTNER_2 = Partner(2, "Partner2")

        val ASCEND = Ascend(
            1, ROUTE.id, AscendStyle.eONSIGHT.id, 2020, 6, 15,
            arrayListOf(PARTNER_1.id, PARTNER_2.id), "Great day"
        )
        val ORPHAN_ASCEND = Ascend(
            2, DatabaseWrapper.INVALID_ID, AscendStyle.eFOLLOWED.id, 2021, 1, 2, arrayListOf(), null
        )

        fun setUpDb(context: Context): DatabaseWrapper {
            val db = DatabaseWrapper(context)
            cleanDb(db)
            db.addCountries(listOf(Country("Country1")))
            db.addRegions(listOf(REGION))
            db.addSectors(listOf(SECTOR))
            db.addRocks(listOf(ROCK))
            db.addRoutes(listOf(ROUTE))
            db.addPartners(listOf(PARTNER_1, PARTNER_2))
            db.addAscends(listOf(ASCEND, ORPHAN_ASCEND))
            return db
        }

        fun cleanDb(db: DatabaseWrapper) {
            db.deleteAscends()
            db.deletePartners()
            db.deleteCountriesRecursively()
        }
    }
}
