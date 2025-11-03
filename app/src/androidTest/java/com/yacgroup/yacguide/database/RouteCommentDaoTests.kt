/*
 * Copyright (C) 2023 Axel Paetzold
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
import com.yacgroup.yacguide.*
import com.yacgroup.yacguide.database.TestDB.Companion.COUNTRIES
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_ID
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_NAME
import com.yacgroup.yacguide.database.TestDB.Companion.REGIONS
import com.yacgroup.yacguide.database.TestDB.Companion.ROCKS
import com.yacgroup.yacguide.database.TestDB.Companion.ROUTES
import com.yacgroup.yacguide.database.TestDB.Companion.ROUTE_COMMENTS
import com.yacgroup.yacguide.database.TestDB.Companion.SECTORS
import com.yacgroup.yacguide.database.comment.RouteCommentDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RouteCommentDaoTests {

    private lateinit var _db: AppDatabase
    private lateinit var _routeCommentDao: RouteCommentDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _routeCommentDao = _db.routeCommentDao()

        TestDB.initRegions(_db.regionDao())
        TestDB.initSectors(_db.sectorDao())
        TestDB.initRocks(_db.rockDao())
        TestDB.initRoutes(_db.routeDao())
        TestDB.initRouteComments(_routeCommentDao)
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAll_invalidRouteId_returnsEmptyList() = runTest {
        assertTrue(_routeCommentDao.getAll(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAll_routeAvailable_returnsCorrespondingRouteComments() = runTest {
        val routeId = ROUTE_COMMENTS.first().routeId
        val routeComments = ROUTE_COMMENTS.filter {
            it.routeId == routeId
        }
        assertTrue(equal(routeComments, _routeCommentDao.getAll(routeId)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_invalidRegionId_returnsEmptyList() = runTest {
        assertTrue(_routeCommentDao.getAllInRegion(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_noRoutesAvailable_returnsEmptyList() = runTest {
        assertTrue(_routeCommentDao.getAllInRegion(REGIONS.last().id).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_routesAvailable_returnsCorrespondingRouteComments() = runTest {
        val regionId = REGIONS.first().id
        val routeComments = ROUTE_COMMENTS.filter { comment ->
            ROUTES.any { route ->
                route.id == comment.routeId && ROCKS.any { rock ->
                    rock.id == route.parentId && SECTORS.any {
                        it.id == rock.parentId && it.parentId == regionId
                    }
                }
            }
        }
        assertTrue(equal(routeComments, _routeCommentDao.getAllInRegion(regionId)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_invalidCountryName_returnsEmptyList() = runTest {
        assertTrue(_routeCommentDao.getAllInCountry(INVALID_NAME).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_noRoutesAvailable_returnsEmptyList() = runTest {
        assertTrue(_routeCommentDao.getAllInCountry(COUNTRIES.last().name).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_routesAvailable_returnsCorrespondingRouteComments() = runTest {
        val countryName = COUNTRIES.first().name
        val routeComments = ROUTE_COMMENTS.filter { comment ->
            ROUTES.any { route ->
                route.id == comment.routeId && ROCKS.any { rock ->
                    rock.id == route.parentId && SECTORS.any { sector ->
                        sector.id == rock.parentId && REGIONS.any {
                            it.id == sector.parentId && it.country == countryName
                        }
                    }
                }
            }
        }
        assertTrue(equal(routeComments, _routeCommentDao.getAllInCountry(countryName)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getCommentCount_returnsNumberOfCommentsForRoute() = runTest {
        ROUTES.forEach { route ->
            val comments = ROUTE_COMMENTS.filter { it.routeId == route.id }
            assertEquals(comments.size, _routeCommentDao.getCommentCount(route.id))
        }
    }
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RouteCommentDaoDeletionTests {

    private lateinit var _db: AppDatabase
    private lateinit var _routeCommentDao: RouteCommentDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _routeCommentDao = _db.routeCommentDao()
    }

    @BeforeEach
    fun init() {
        TestDB.initRouteComments(_routeCommentDao)
    }

    @AfterEach
    fun clear() {
        _db.clearAllTables()
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAll_routeCommentTableBecomesEmpty() = runTest {
        _routeCommentDao.deleteAll()
        assertTrue(_routeCommentDao.all.isEmpty())
    }
}
