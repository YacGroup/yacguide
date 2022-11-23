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
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_NAME
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

        TestDB.initCountries(_db.countryDao())
        TestDB.initRegions(_regionDao)
        TestDB.initSectors(_db.sectorDao())
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @Test
    fun getAllInCountry_invalidCountryName_returnsEmptyList() = runBlockingTest {
        assertTrue(_regionDao.getAllInCountry(INVALID_NAME).isEmpty())
    }

    @Test
    fun getAllInCountry_noRegionsAvailable_returnsEmptyList() = runBlockingTest {
        assertTrue(_regionDao.getAllInCountry(COUNTRIES.last().name).isEmpty())
    }

    @Test
    fun getAllInCountry_regionsAvailable_returnsCorrespondingRegions() = runBlockingTest {
        val country1Name = COUNTRIES.first().name
        val regionsInCountry1 = REGIONS.filter {
            it.country == country1Name
        }
        assertTrue(equal(regionsInCountry1, _regionDao.getAllInCountry(country1Name)))
    }

    @Test
    fun getAllNonEmpty_sectorsWithParentRegionsAvailable_returnsCorrespondingRegions() = runBlockingTest {
        val nonEmptyRegions = REGIONS.filter { region ->
            SECTORS.any { it.parentId == region.id }
        }
        assertTrue(equal(nonEmptyRegions, _regionDao.getAllNonEmpty()))
    }

    @Test
    fun getRegion_invalidRegionId_returnsNull() = runBlockingTest {
        assertNull(_regionDao.getRegion(INVALID_ID))
    }

    @Test
    fun getRegion_regionAvailable_returnsRegion() = runBlockingTest {
        val region = REGIONS.first()
        assertTrue(_regionDao.getRegion(region.id) == region)
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
    fun deleteAll_regionTableBecomesEmpty() = runBlockingTest {
        _regionDao.deleteAll()
        assertTrue(_regionDao.all.isEmpty())
    }

    @Test
    fun deleteAll_countryNameGiven_regionTableDoesNotContainCorrespondingRegionsAnymore() = runBlockingTest {
        val country1Name = COUNTRIES.first().name
        val regionsInCountry1 = REGIONS.filter {
            it.country == country1Name
        }
        _regionDao.deleteAll(country1Name)
        regionsInCountry1.forEach {
            assertFalse(_regionDao.all.contains(it))
        }
    }
}
