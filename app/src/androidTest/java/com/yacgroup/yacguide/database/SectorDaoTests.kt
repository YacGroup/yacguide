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
import com.yacgroup.yacguide.database.TestDB.Companion.COUNTRIES
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_ID
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_NAME
import com.yacgroup.yacguide.database.TestDB.Companion.REGIONS
import com.yacgroup.yacguide.database.TestDB.Companion.SECTORS
import com.yacgroup.yacguide.equal
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SectorDaoTests {

    private lateinit var _db: AppDatabase
    private lateinit var _sectorDao: SectorDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _sectorDao = _db.sectorDao()
    }

    @BeforeEach
    fun init() {
        TestDB.initSectors(_sectorDao)
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
    fun getAllInRegion_invalidRegionId_returnsEmptyList() = runBlockingTest {
        assertTrue(_sectorDao.getAllInRegion(INVALID_ID).isEmpty())
    }

    @Test
    fun getAllInRegion_noSectorsAvailable_returnsEmptyList() = runBlockingTest {
        assertTrue(_sectorDao.getAllInRegion(REGIONS.last().id).isEmpty())
    }

    @Test
    fun getAllInRegion_sectorsAvailable_returnsCorrespondingSectors() = runBlockingTest {
        val region1Id = REGIONS.first().id
        val sectorsInRegion1 = SECTORS.filter {
            it.parentId == region1Id
        }
        assertTrue(equal(sectorsInRegion1, _sectorDao.getAllInRegion(region1Id)))
    }

    @Test
    fun getAllInCountry_invalidCountryName_returnsEmptyList() = runBlockingTest {
        TestDB.initRegions(_db.regionDao())

        assertTrue(_sectorDao.getAllInCountry(INVALID_NAME).isEmpty())
    }

    @Test
    fun getAllInCountry_noSectorsAvailable_returnsEmptyList() = runBlockingTest {
        TestDB.initRegions(_db.regionDao())

        assertTrue(_sectorDao.getAllInCountry(COUNTRIES.last().name).isEmpty())
    }

    @Test
    fun getAllInCountry_sectorsAvailable_returnsCorrespondingSectors() = runBlockingTest {
        TestDB.initRegions(_db.regionDao())

        val country1Name = COUNTRIES.first().name
        val sectorsInCountry1 = SECTORS.filter { sectorIt ->
            REGIONS.any { it.id == sectorIt.parentId && it.country == country1Name }
        }

        assertTrue(equal(sectorsInCountry1, _sectorDao.getAllInCountry(country1Name)))
    }

    @Test
    fun getSector_invalidSectorId_returnsNull() = runBlockingTest {
        assertNull(_sectorDao.getSector(INVALID_ID))
    }

    @Test
    fun getSector_sectorAvailable_returnsSector() = runBlockingTest {
        val sector = SECTORS.first()

        assertTrue(_sectorDao.getSector(sector.id) == sector)
    }

    @Test
    fun deleteAll_sectorTableBecomesEmpty() = runBlockingTest {
        _sectorDao.deleteAll()

        assertTrue(_sectorDao.all.isEmpty())
    }

    @Test
    fun deleteAll_regionIdGiven_sectorTableDoesNotContainCorrespondingSectorsAnymore() {
        val region1Id = REGIONS.first().id
        val sectorsInRegion1 = SECTORS.filter {
            it.parentId == region1Id
        }
        _sectorDao.deleteAll(region1Id)

        sectorsInRegion1.forEach{
            assertFalse(_sectorDao.all.contains(it))
        }
    }
}