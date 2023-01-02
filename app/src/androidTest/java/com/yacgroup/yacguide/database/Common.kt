/*
 * Copyright (C) 2022, 2023 Axel Paetzold
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

package com.yacgroup.yacguide.database

import com.yacgroup.yacguide.database.comment.RockComment
import com.yacgroup.yacguide.database.comment.RockCommentDao
import com.yacgroup.yacguide.database.comment.RouteComment
import com.yacgroup.yacguide.database.comment.RouteCommentDao
import com.yacgroup.yacguide.equal
import com.yacgroup.yacguide.utils.AscendStyle
import org.junit.jupiter.api.Assertions.assertTrue

/*********************************************************************
 * Database structure:
 * - Country1:
 *      - Region1
 *          - Sector1
 *              - Rock1
 *                  - RockComment1
 *                  - RockComment2
 *                  - Route1
 *                      - RouteComment1
 *                      - RouteComment2
 *                      - Ascend1
 *                      - Ascend2
 *                  - Route2
 *                      - RouteComment3
 *                      - Ascend3
 *              - Rock2
 *                  - RockComment3
 *                  - Route3
 *          - Sector2
 *              - Rock3
 *      - Region2
 *          - Sector3
 * - Country2
 *      - Region3
 * - Country3
 ********************************************************************/
class TestDB {

    companion object {
        const val INVALID_NAME = "???"
        const val INVALID_ID = 42

        val COUNTRIES = listOf(
            Country("Country1"),
            Country("Country2"),
            Country("Country3")
        )

        val REGIONS = listOf(
            Region(1, "Region1", "Country1"),
            Region(2, "Region2", "Country1"),
            Region(3, "Region3", "Country2")
        )

        val SECTORS = listOf(
            Sector(1, 1f, "Sector1", REGIONS[0].id),
            Sector(2, 2f, "Sector2", REGIONS[0].id),
            Sector(3, 3f, "Sector3", REGIONS[1].id)
        )

        val ROCKS = listOf(
            Rock(1, 1f, 'G', ' ', "Rock1", 0f, 0f, 0, SECTORS[0].id),
            Rock(2, 2f, 'G', ' ', "Rock2", 0f, 0f, 0, SECTORS[0].id),
            Rock(3, 3f, 'G', ' ', "Rock3", 0f, 0f, 0, SECTORS[1].id)
        )

        val ROCK_COMMENTS = listOf(
            RockComment(1, 1, "RockComment1", ROCKS[0].id),
            RockComment(2, 3, "RockComment2", ROCKS[0].id),
            RockComment(3, 4, "RockComment3", ROCKS[1].id)
        )

        val ROUTES = listOf(
            Route(1, 1f, 0, "Route1", "Grade1", "Leader1", "Follower1", "Date1", "ClimbingType1", "Description1", 0, ROCKS[0].id),
            Route(2, 2f, 0, "Route2", "Grade2", "Leader2", "Follower2", "Date2", "ClimbingType2", "Description2", 0, ROCKS[0].id),
            Route(3, 3f, 0, "Route3", "Grade3", "Leader3", "Follower3", "Date3", "ClimbingType3", "Description3", 0, ROCKS[1].id)
        )

        val ROUTE_COMMENTS = listOf(
            RouteComment(1, 3, 3, 3, 5, "RouteComment1", ROUTES[0].id),
            RouteComment(2, 5, 5, 5, 7, "RouteComment2", ROUTES[0].id),
            RouteComment(3, 2, 2, 2, 13, "RouteComment3", ROUTES[1].id)
        )

        val ASCENDS = listOf(
            Ascend(1, ROUTES[0].id, AscendStyle.eONSIGHT.id, 2000, 10, 10, ArrayList(), ""),
            Ascend(2, ROUTES[0].id, AscendStyle.eFOLLOWED.id, 0, 0, 0, ArrayList(), ""),
            Ascend(3, ROUTES[1].id, AscendStyle.ePROJECT.id, 2000, 3, 3, ArrayList(), "")
        )

        val PARTNERS = listOf(
            Partner(1, "Partner1"),
            Partner(2, "Partner2"),
            Partner(3, "Partner3")
        )

        fun initCountries(countryDao: CountryDao) {
            assertTrue(countryDao.all.isEmpty())
            countryDao.insert(COUNTRIES)
            assertTrue(equal(COUNTRIES, countryDao.all))
        }

        fun initRegions(regionDao: RegionDao) {
            assertTrue(regionDao.all.isEmpty())
            regionDao.insert(REGIONS)
            assertTrue(equal(REGIONS, regionDao.all))
        }

        fun initSectors(sectorDao: SectorDao) {
            assertTrue(sectorDao.all.isEmpty())
            sectorDao.insert(SECTORS)
            assertTrue(equal(SECTORS, sectorDao.all))
        }

        fun initRocks(rockDao: RockDao) {
            assertTrue(rockDao.all.isEmpty())
            rockDao.insert(ROCKS)
            assertTrue(equal(ROCKS, rockDao.all))
        }

        fun initRockComments(rockCommentDao: RockCommentDao) {
            assertTrue(rockCommentDao.all.isEmpty())
            rockCommentDao.insert(ROCK_COMMENTS)
            assertTrue(equal(ROCK_COMMENTS, rockCommentDao.all))
        }

        fun initRoutes(routeDao: RouteDao) {
            assertTrue(routeDao.all.isEmpty())
            routeDao.insert(ROUTES)
            assertTrue(equal(ROUTES, routeDao.all))
        }

        fun initRouteComments(routeCommentDao: RouteCommentDao) {
            assertTrue(routeCommentDao.all.isEmpty())
            routeCommentDao.insert(ROUTE_COMMENTS)
            assertTrue(equal(ROUTE_COMMENTS, routeCommentDao.all))
        }

        fun initAscends(ascendDao: AscendDao) {
            assertTrue(ascendDao.all.isEmpty())
            ascendDao.insert(ASCENDS)
            assertTrue(equal(ASCENDS, ascendDao.all))
        }

        fun initPartners(partnerDao: PartnerDao) {
            assertTrue(partnerDao.all.isEmpty())
            partnerDao.insert(PARTNERS)
            assertTrue(equal(PARTNERS, partnerDao.all))
        }
    }
}