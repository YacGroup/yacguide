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

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yacgroup.yacguide.*
import com.yacgroup.yacguide.database.TestDB.Companion.COUNTRIES
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_ID
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_NAME
import com.yacgroup.yacguide.database.TestDB.Companion.REGIONS
import com.yacgroup.yacguide.database.TestDB.Companion.ROUTES
import com.yacgroup.yacguide.database.TestDB.Companion.SECTORS
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegionDaoTests {

    private lateinit var _db: AppDatabase
    private lateinit var _regionDao: RegionDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _regionDao = _db.regionDao()

        TestDB.initCountries(_db.countryDao())
        TestDB.initRegions(_regionDao)
        TestDB.initSectors(_db.sectorDao())
        TestDB.initRocks(_db.rockDao())
        TestDB.initRoutes(_db.routeDao())
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @Test
    fun getAllInCountry_invalidCountryName_returnsEmptyList() = runTest {
        assertTrue(_regionDao.getAllInCountry(INVALID_NAME).isEmpty())
    }

    @Test
    fun getAllInCountry_noRegionsAvailable_returnsEmptyList() = runTest {
        assertTrue(_regionDao.getAllInCountry(COUNTRIES.last().name).isEmpty())
    }

    @Test
    fun getAllInCountry_regionsAvailable_returnsCorrespondingRegions() = runTest {
        val country1Name = COUNTRIES.first().name
        val regionsInCountry1 = REGIONS.filter {
            it.country == country1Name
        }
        assertTrue(equal(regionsInCountry1, _regionDao.getAllInCountry(country1Name)))
    }

    @Test
    fun getAllNonEmpty_sectorsWithParentRegionsAvailable_returnsCorrespondingRegions() = runTest {
        val nonEmptyRegions = REGIONS.filter { region ->
            SECTORS.any { it.parentId == region.id }
        }
        assertTrue(equal(nonEmptyRegions, _regionDao.getAllNonEmpty()))
    }

    @Test
    fun getRegion_invalidRegionId_returnsNull() = runTest {
        assertNull(_regionDao.getRegion(INVALID_ID))
    }

    @Test
    fun getRegion_regionAvailable_returnsRegion() = runTest {
        val region = REGIONS.first()
        assertEquals(_regionDao.getRegion(region.id), region)
    }

    @Test
    fun getRegionForRoute_invalidRouteId_returnsNull() = runTest {
        assertNull(_regionDao.getRegionForRoute(INVALID_ID))
    }

    @Test
    fun getRegionForRoute_regionForRouteAvailable_returnsAccordingRegion() = runTest {
        val region = REGIONS.first()
        assertEquals(region, _regionDao.getRegionForRoute(ROUTES.first().id))
    }
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegionDaoDeletionTests {

    private lateinit var _db: AppDatabase
    private lateinit var _regionDao: RegionDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _regionDao = _db.regionDao()
    }

    @BeforeEach
    fun init() {
        TestDB.initRegions(_regionDao)
    }

    @AfterEach
    fun clear() {
        _db.clearAllTables()
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @Test
    fun deleteAll_regionTableBecomesEmpty() = runTest {
        _regionDao.deleteAll()
        assertTrue(_regionDao.all.isEmpty())
    }

}
