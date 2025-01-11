/*
 * Copyright (C) 2022, 2023, 2025 Axel Paetzold
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

import com.yacgroup.yacguide.database.comment.*
import com.yacgroup.yacguide.equal
import com.yacgroup.yacguide.utils.AscendStyle
import org.junit.jupiter.api.Assertions.assertTrue

/*********************************************************************
 * Database structure:
 * - Country1:
 *      - Region1
 *          - RegionComment1
 *          - RegionComment2
 *          - Sector1
 *              - SectorComment1
 *              - SectorComment2
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
 *                      - Ascend 4
 *              - Rock 3
 *                  - Route 4
 *                      - Ascend 5
 *              - Rock 4
 *                  - Route 5
 *                      - Ascend 6
 *                  - Route 6
 *              - Rock 5
 *          - Sector2
 *              - SectorComment3
 *              - Rock3
 *      - Region2
 *          - RegionComment3
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
            Region(1, "Region1", COUNTRIES[0].name),
            Region(2, "Region2", COUNTRIES[0].name),
            Region(3, "Region3", COUNTRIES[1].name)
        )

        val REGION_COMMENTS = listOf(
            RegionComment(1, 0, "RegionComment1", REGIONS[0].id),
            RegionComment(2, 0, "RegionComment2", REGIONS[0].id),
            RegionComment(3, 0, "RegionComment3", REGIONS[1].id)
        )

        val SECTORS = listOf(
            Sector(1, 1f, "Sector1", REGIONS[0].id),
            Sector(2, 2f, "Sector2", REGIONS[0].id),
            Sector(3, 3f, "Sector3", REGIONS[1].id)
        )

        val SECTOR_COMMENTS = listOf(
            SectorComment(1, 0, "SectorComment1", SECTORS[0].id),
            SectorComment(2, 0, "SectorComment2", SECTORS[0].id),
            SectorComment(3, 0, "SectorComment3", SECTORS[1].id)
        )

        val ROCKS = listOf(
            Rock(1, 1f, 'G', ' ', "Rock1", 0f, 0f, AscendStyle.bitMask(AscendStyle.eONSIGHT.id) or AscendStyle.bitMask(AscendStyle.eFOLLOWED.id), SECTORS[0].id),
            Rock(2, 2f, 'M', 'X', "Rock2", 0f, 0f, AscendStyle.bitMask(AscendStyle.eFOLLOWED.id), SECTORS[0].id),
            Rock(3, 3f, 'B', ' ', "Rock3", 0f, 0f, AscendStyle.bitMask(AscendStyle.eALTERNATINGLEADS.id), SECTORS[1].id),
            Rock(4, 4f, 'G', ' ', "Rock4", 0f, 0f, 0, SECTORS[1].id),
            Rock(5, 5f, 'G', ' ', "Rock5", 0f, 0f, 0, SECTORS[1].id),
        )

        val ROCK_COMMENTS = listOf(
            RockComment(1, 1, "RockComment1", ROCKS[0].id),
            RockComment(2, 3, "RockComment2", ROCKS[0].id),
            RockComment(3, 4, "RockComment3", ROCKS[1].id)
        )

        val ROUTES = listOf(
            Route(1, 1f, 0, "Route1", "Grade1", "Leader1", "Follower1", "Date1", "ClimbingType1", "Description1", AscendStyle.bitMask(AscendStyle.eONSIGHT.id) or AscendStyle.eFOLLOWED.id, ROCKS[0].id),
            Route(2, 2f, 0, "Route2", "Grade2", "Leader2", "Follower2", "Date2", "ClimbingType2", "Description2", AscendStyle.bitMask(AscendStyle.eFOLLOWED.id), ROCKS[0].id),
            Route(3, 3f, 0, "Route3", "Grade3", "Leader3", "Follower3", "Date3", "ClimbingType3", "Description3", ROCKS[1].ascendsBitMask, ROCKS[1].id),
            Route(4, 4f, 0, "Route4", "Grade4", "Leader4", "Follower4", "Date4", "ClimbingType4", "Description4", ROCKS[2].ascendsBitMask, ROCKS[2].id),
            Route(5, 5f, 0, "Route5", "Grade5", "Leader5", "Follower5", "Date5", "ClimbingType5", "Description5", AscendStyle.bitMask(AscendStyle.ePROJECT.id), ROCKS[3].id),
            Route(6, 6f, 0, "Route6", "Grade6", "Leader6", "Follower6", "Date6", "ClimbingType6", "Description6", 0, ROCKS[3].id)
        )

        val ROUTE_COMMENTS = listOf(
            RouteComment(1, 3, 3, 3, 5, "RouteComment1", ROUTES[0].id),
            RouteComment(2, 5, 5, 5, 7, "RouteComment2", ROUTES[0].id),
            RouteComment(3, 2, 2, 2, 13, "RouteComment3", ROUTES[1].id)
        )

        val ASCENDS = listOf(
            Ascend(1, ROUTES[0].id, AscendStyle.eONSIGHT.id, 2000, 10, 10, ArrayList(), ""),
            Ascend(2, ROUTES[0].id, AscendStyle.eFOLLOWED.id, 0, 0, 0, ArrayList(), ""),
            Ascend(3, ROUTES[1].id, AscendStyle.eFOLLOWED.id, 1990, 3, 5, ArrayList(), ""),
            Ascend(4, ROUTES[2].id, AscendStyle.eFOLLOWED.id, 1996, 3, 3, ArrayList(), ""),
            Ascend(5, ROUTES[3].id, AscendStyle.eALTERNATINGLEADS.id, 1996, 12, 12, ArrayList(), ""),
            Ascend(6, ROUTES[4].id, AscendStyle.ePROJECT.id, 2000, 12, 12, ArrayList(), "")
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

        fun initRegionComments(regionCommentDao: RegionCommentDao) {
            assertTrue(regionCommentDao.all.isEmpty())
            regionCommentDao.insert(REGION_COMMENTS)
            assertTrue(equal(REGION_COMMENTS, regionCommentDao.all))
        }

        fun initSectors(sectorDao: SectorDao) {
            assertTrue(sectorDao.all.isEmpty())
            sectorDao.insert(SECTORS)
            assertTrue(equal(SECTORS, sectorDao.all))
        }

        fun initSectorComments(sectorCommentDao: SectorCommentDao) {
            assertTrue(sectorCommentDao.all.isEmpty())
            sectorCommentDao.insert(SECTOR_COMMENTS)
            assertTrue(equal(SECTOR_COMMENTS, sectorCommentDao.all))
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