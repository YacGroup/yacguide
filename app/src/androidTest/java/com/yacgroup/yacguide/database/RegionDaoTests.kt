/*
 * Copyright (C) 2022 Axel Paetzold
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
import com.yacgroup.yacguide.database.TestDB.Companion.REGIONS
import com.yacgroup.yacguide.database.TestDB.Companion.SECTORS
import kotlinx.coroutines.test.runBlockingTest
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
    fun getAllNonEmpty_noSectorsAvailable_returnsEmptyList() = runBlockingTest {
        TestDB.initCountries(_db.countryDao())
        TestDB.initRegions(_regionDao)

        assertTrue(_regionDao.getAllNonEmpty().isEmpty())
    }

    @Test
    fun getAllNonEmpty_sectorsWithParentRegionsAvailable_returnsCorrespondingRegions() = runBlockingTest {
        TestDB.initCountries(_db.countryDao())
        TestDB.initRegions(_regionDao)
        TestDB.initSectors(_db.sectorDao())

        val nonEmptyRegions = REGIONS.filter { regionIt ->
            SECTORS.any { it.parentId == regionIt.id }
        }
        assertTrue(equal(nonEmptyRegions, _regionDao.getAllNonEmpty()))
    }

    @Test
    fun getRegion_invalidRegionId_returnsNull() = runBlockingTest {
        TestDB.initRegions(_regionDao)

        assertNull(_regionDao.getRegion(INVALID_ID))
    }

    @Test
    fun getRegion_regionAvailable_returnsRegion() = runBlockingTest {
        TestDB.initRegions(_regionDao)

        val region = REGIONS.first()
        assertTrue(_regionDao.getRegion(region.id) == region)
    }

    @Test
    fun deleteAll_regionTableBecomesEmpty() = runBlockingTest {
        TestDB.initRegions(_regionDao)

        _regionDao.deleteAll()
        assertTrue(_regionDao.all.isEmpty())
    }

    @Test
    fun deleteAll_countryNameGiven_regionTableDoesNotContainCorrespondingRegionsAnymore() {
        TestDB.initRegions(_regionDao)

        val country1Name = COUNTRIES.first().name
        val regionsInCountry1 = REGIONS.filter {
            it.country == country1Name
        }
        _regionDao.deleteAll(country1Name)

        regionsInCountry1.forEach{
            assertFalse(_regionDao.all.contains(it))
        }
    }
}
