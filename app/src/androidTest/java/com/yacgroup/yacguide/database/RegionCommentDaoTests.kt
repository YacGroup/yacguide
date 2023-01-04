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
import com.yacgroup.yacguide.database.TestDB.Companion.REGION_COMMENTS
import com.yacgroup.yacguide.database.comment.RegionCommentDao
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegionCommentDaoTests {

    private lateinit var _db: AppDatabase
    private lateinit var _regionCommentDao: RegionCommentDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _regionCommentDao = _db.regionCommentDao()

        TestDB.initRegions(_db.regionDao())
        TestDB.initRegionComments(_regionCommentDao)
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @Test
    fun getAll_invalidRegionId_returnsEmptyList() = runTest {
        assertTrue(_regionCommentDao.getAll(INVALID_ID).isEmpty())
    }

    @Test
    fun getAll_regionAvailable_returnsCorrespondingRegionComments() = runTest {
        val regionId = REGION_COMMENTS.first().regionId
        val regionComments = REGION_COMMENTS.filter {
            it.regionId == regionId
        }
        assertTrue(equal(regionComments, _regionCommentDao.getAll(regionId)))
    }

    @Test
    fun getAllInCountry_invalidCountryName_returnsEmptyList() = runTest {
        assertTrue(_regionCommentDao.getAllInCountry(INVALID_NAME).isEmpty())
    }

    @Test
    fun getAllInCountry_noRegionsAvailable_returnsEmptyList() = runTest {
        assertTrue(_regionCommentDao.getAllInCountry(COUNTRIES.last().name).isEmpty())
    }

    @Test
    fun getAllInCountry_regionsAvailable_returnsCorrespondingRegions() = runTest {
        val countryName = COUNTRIES.first().name
        val regionComments = REGION_COMMENTS.filter { comment ->
            REGIONS.any {
                it.id == comment.regionId && it.country == countryName
            }
        }
        assertTrue(equal(regionComments, _regionCommentDao.getAllInCountry(countryName)))
    }
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegionCommentDaoDeletionTests {

    private lateinit var _db: AppDatabase
    private lateinit var _regionCommentDao: RegionCommentDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _regionCommentDao = _db.regionCommentDao()
    }

    @BeforeEach
    fun init() {
        TestDB.initRegionComments(_regionCommentDao)
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
    fun deleteAll_regionCommentsTableBecomesEmpty() = runTest {
        _regionCommentDao.deleteAll()
        assertTrue(_regionCommentDao.all.isEmpty())
    }

    @Test
    fun deleteAll_regionIdGiven_regionCommentsTableDoesNotContainCorrespondingCommentsAnymore() = runTest {
        val regionId = REGION_COMMENTS.first().regionId
        val regionComments = REGION_COMMENTS.filter {
            it.regionId == regionId
        }
        _regionCommentDao.deleteAll(regionId)
        regionComments.forEach {
            assertFalse(_regionCommentDao.all.contains(it))
        }
    }
}
