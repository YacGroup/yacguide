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
import com.yacgroup.yacguide.database.TestDB.Companion.SECTORS
import com.yacgroup.yacguide.database.TestDB.Companion.SECTOR_COMMENTS
import com.yacgroup.yacguide.database.comment.SectorCommentDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SectorCommentDaoTests {

    private lateinit var _db: AppDatabase
    private lateinit var _sectorCommentDao: SectorCommentDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _sectorCommentDao = _db.sectorCommentDao()

        TestDB.initRegions(_db.regionDao())
        TestDB.initSectors(_db.sectorDao())
        TestDB.initSectorComments(_sectorCommentDao)
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAll_invalidSectorId_returnsEmptyList() = runTest {
        assertTrue(_sectorCommentDao.getAll(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAll_sectorAvailable_returnsCorrespondingSectors() = runTest {
        val sectorId = SECTOR_COMMENTS.first().sectorId
        val sectorComments = SECTOR_COMMENTS.filter {
            it.sectorId == sectorId
        }
        assertTrue(equal(sectorComments, _sectorCommentDao.getAll(sectorId)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_invalidRegionId_returnsEmptyList() = runTest {
        assertTrue(_sectorCommentDao.getAllInRegion(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_noSectorsAvailable_returnsEmptyList() = runTest {
        assertTrue(_sectorCommentDao.getAllInRegion(REGIONS.last().id).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_sectorsAvailable_returnsCorrespondingSectors() = runTest {
        val regionId = REGIONS.first().id
        val sectorComments = SECTOR_COMMENTS.filter { comment ->
            SECTORS.any {
                it.id == comment.sectorId && it.parentId == regionId
            }
        }
        assertTrue(equal(sectorComments, _sectorCommentDao.getAllInRegion(regionId)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_invalidCountryName_returnsEmptyList() = runTest {
        assertTrue(_sectorCommentDao.getAllInCountry(INVALID_NAME).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_noSectorsAvailable_returnsEmptyList() = runTest {
        assertTrue(_sectorCommentDao.getAllInCountry(COUNTRIES.last().name).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_sectorsAvailable_returnsCorrespondingSectors() = runTest {
        val countryName = COUNTRIES.first().name
        val sectorComments = SECTOR_COMMENTS.filter { comment ->
            SECTORS.any { sector ->
                sector.id == comment.sectorId && REGIONS.any {
                    it.id == sector.parentId && it.country == countryName
                }
            }
        }
        assertTrue(equal(sectorComments, _sectorCommentDao.getAllInCountry(countryName)))
    }
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SectorCommentDaoDeletionTests {

    private lateinit var _db: AppDatabase
    private lateinit var _sectorCommentDao: SectorCommentDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _sectorCommentDao = _db.sectorCommentDao()
    }

    @BeforeEach
    fun init() {
        TestDB.initSectorComments(_sectorCommentDao)
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
    fun deleteAll_sectorTableBecomesEmpty() = runTest {
        _sectorCommentDao.deleteAll()
        assertTrue(_sectorCommentDao.all.isEmpty())
    }
}
