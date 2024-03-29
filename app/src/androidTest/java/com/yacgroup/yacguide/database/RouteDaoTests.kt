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

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yacgroup.yacguide.database.TestDB.Companion.ASCENDS
import com.yacgroup.yacguide.database.TestDB.Companion.COUNTRIES
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_ID
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_NAME
import com.yacgroup.yacguide.database.TestDB.Companion.REGIONS
import com.yacgroup.yacguide.database.TestDB.Companion.ROCKS
import com.yacgroup.yacguide.database.TestDB.Companion.ROUTES
import com.yacgroup.yacguide.database.TestDB.Companion.ROUTE_COMMENTS
import com.yacgroup.yacguide.database.TestDB.Companion.SECTORS
import com.yacgroup.yacguide.database.comment.RouteComment
import com.yacgroup.yacguide.database.comment.RouteComment.Companion.NO_INFO_ID
import com.yacgroup.yacguide.equal
import com.yacgroup.yacguide.utils.AscendStyle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RouteDaoTests {

    private lateinit var _db: AppDatabase
    private lateinit var _routeDao: RouteDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _routeDao = _db.routeDao()

        TestDB.initCountries(_db.countryDao())
        TestDB.initRegions(_db.regionDao())
        TestDB.initSectors(_db.sectorDao())
        TestDB.initRocks(_db.rockDao())
        TestDB.initRoutes(_routeDao)
        TestDB.initRouteComments(_db.routeCommentDao())
        TestDB.initAscends(_db.ascendDao())
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllByName_nameNotAvailable_returnsEmptyList() = runTest {
        assertTrue(_routeDao.getAllByName(INVALID_NAME).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllByName_nameAvailable_returnsCorrespondingRoutes() = runTest {
        val route = ROUTES.first()
        assertTrue(equal(listOf(route), _routeDao.getAllByName(route.name!!)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllByGrade_returnsOnlyRoutesInRequestedGradeRange() = runTest {
        RouteComment.GRADE_MAP.keys.forEach { gradeId ->
            val routes = ROUTES.filter { route ->
                val comments = ROUTE_COMMENTS.filter {
                    it.routeId == route.id
                }
                comments.isNotEmpty() && (comments.sumOf { it.gradeId.toDouble() / comments.size } in NO_INFO_ID.toDouble()..gradeId.toDouble())
            }
            assertTrue(equal(routes, _routeDao.getAllByGrade(NO_INFO_ID, gradeId)))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllByQuality_returnsOnlyRoutesOfRequestedOrBetterQuality() = runTest {
        RouteComment.QUALITY_MAP.keys.forEach { maxQualityId ->
            val routes = ROUTES.filter { route ->
                val comments = ROUTE_COMMENTS.filter {
                    it.routeId == route.id
                }
                comments.isNotEmpty() && (comments.sumOf { it.qualityId.toDouble() / comments.size } <= maxQualityId)
            }
            assertTrue(equal(routes, _routeDao.getAllByQuality(maxQualityId)))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllByProtection_returnsOnlyRoutesOfRequestedOrBetterProtection() = runTest {
        RouteComment.PROTECTION_MAP.keys.forEach { maxProtectionId ->
            val routes = ROUTES.filter { route ->
                val comments = ROUTE_COMMENTS.filter {
                    it.routeId == route.id
                }
                comments.isNotEmpty() && (comments.sumOf { it.securityId.toDouble() / comments.size } <= maxProtectionId)
            }
            assertTrue(equal(routes, _routeDao.getAllByProtection(maxProtectionId)))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllByDrying_returnsOnlyRoutesOfRequestedOrBetterDrying() = runTest {
        RouteComment.DRYING_MAP.keys.forEach { maxDryingId ->
            val routes = ROUTES.filter { route ->
                val comments = ROUTE_COMMENTS.filter {
                    it.routeId == route.id
                }
                comments.isNotEmpty() && (comments.sumOf { it.wetnessId.toDouble() / comments.size } <= maxDryingId)
            }
            assertTrue(equal(routes, _routeDao.getAllByDrying(maxDryingId)))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllForStyle_returnsOnlyRoutesAscendedWithRequestedStyle() = runTest {
        AscendStyle.values().forEach { style ->
            val routes = ROUTES.filter { route ->
                ASCENDS.any {
                    it.routeId == route.id && it.styleId == style.id
                }
            }
            assertTrue(equal(routes, _routeDao.getAllForStyle(style.id)))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_invalidCountryName_returnsEmptyList() = runTest {
        assertTrue(_routeDao.getAllInCountry(INVALID_NAME).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_noRoutesAvailable_returnsEmptyList() = runTest {
        assertTrue(_routeDao.getAllInCountry(COUNTRIES.last().name).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_routesAvailable_returnsCorrespondingRoutes() = runTest {
        val country1Name = COUNTRIES.first().name
        val routesInCountry1 = ROUTES.filter { route ->
            ROCKS.any { rock ->
                rock.id == route.parentId && SECTORS.any { sector ->
                     sector.id == rock.parentId && REGIONS.any {
                        it.id == sector.parentId && it.country == country1Name
                    }
                }
            }
        }
        assertTrue(equal(routesInCountry1, _routeDao.getAllInCountry(country1Name)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_invalidRegionId_returnsEmptyList() = runTest {
        assertTrue(_routeDao.getAllInRegion(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_noRoutesAvailable_returnsEmptyList() = runTest {
        assertTrue(_routeDao.getAllInRegion(REGIONS.last().id).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_routesAvailable_returnsCorrespondingRoutes() = runTest {
        val region1Id = REGIONS.first().id
        val routesInRegion1 = ROUTES.filter { route ->
            ROCKS.any { rock ->
                rock.id == route.parentId && SECTORS.any {
                    it.id == rock.parentId && it.parentId == region1Id }
            }
        }
        assertTrue(equal(routesInRegion1, _routeDao.getAllInRegion(region1Id)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInSector_invalidSectorId_returnsEmptyList() = runTest {
        assertTrue(_routeDao.getAllInSector(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInSector_noRoutesAvailable_returnsEmptyList() = runTest {
        assertTrue(_routeDao.getAllInSector(SECTORS.last().id).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInSector_routesAvailable_returnsCorrespondingRoutes() = runTest {
        val sector1Id = SECTORS.first().id
        val routesInSector1 = ROUTES.filter { route ->
            ROCKS.any {
                it.id == route.parentId && it.parentId == sector1Id
            }
        }
        assertTrue(equal(routesInSector1, _routeDao.getAllInSector(sector1Id)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllAtRock_invalidRockId_returnsEmptyList() = runTest {
        assertTrue(_routeDao.getAllAtRock(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllAtRock_noRoutesAvailable_returnsEmptyList() = runTest {
        assertTrue(_routeDao.getAllAtRock(ROCKS.last().id).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllAtRock_routesAvailable_returnsCorrespondingRoutes() = runTest {
        val rock1Id = ROCKS.first().id
        val routesAtRock1 = ROUTES.filter {
            it.parentId == rock1Id
        }
        assertTrue(equal(routesAtRock1, _routeDao.getAllAtRock(rock1Id)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllByNameAtRock_nameAvailableAtDifferentRock_returnsEmptyList() = runTest {
        assertTrue(_routeDao.getAllByNameAtRock(ROCKS.first().id, ROUTES.last().name!!).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getRoute_invalidRouteId_returnsNull() = runTest {
        assertNull(_routeDao.getRoute(INVALID_ID))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getRoute_routeAvailable_returnsRoute() = runTest {
        val route = ROUTES.first()
        assertEquals(_routeDao.getRoute(route.id), route)
    }
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RouteDaoDeletionTests {

    private lateinit var _db: AppDatabase
    private lateinit var _routeDao: RouteDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _routeDao = _db.routeDao()

        TestDB.initRoutes(_routeDao)
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAll_routeTableBecomesEmpty() = runTest {
        _routeDao.deleteAll()
        assertTrue(_routeDao.all.isEmpty())
    }
}