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
import com.yacgroup.yacguide.database.TestDB.Companion.ROCK_COMMENTS
import com.yacgroup.yacguide.database.TestDB.Companion.SECTORS
import com.yacgroup.yacguide.database.comment.RockCommentDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RockCommentDaoTests {

    private lateinit var _db: AppDatabase
    private lateinit var _rockCommentDao: RockCommentDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _rockCommentDao = _db.rockCommentDao()

        TestDB.initRegions(_db.regionDao())
        TestDB.initSectors(_db.sectorDao())
        TestDB.initRocks(_db.rockDao())
        TestDB.initRockComments(_rockCommentDao)
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAll_invalidRockId_returnsEmptyList() = runTest {
        assertTrue(_rockCommentDao.getAll(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAll_rockAvailable_returnsCorrespondingRockComments() = runTest {
        val rockId = ROCK_COMMENTS.first().rockId
        val rockComments = ROCK_COMMENTS.filter {
            it.rockId == rockId
        }
        assertTrue(equal(rockComments, _rockCommentDao.getAll(rockId)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_invalidRegionId_returnsEmptyList() = runTest {
        assertTrue(_rockCommentDao.getAllInRegion(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_noRocksAvailable_returnsEmptyList() = runTest {
        assertTrue(_rockCommentDao.getAllInRegion(REGIONS.last().id).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInRegion_rocksAvailable_returnsCorrespondingRockComments() = runTest {
        val regionId = REGIONS.first().id
        val rockComments = ROCK_COMMENTS.filter { comment ->
            ROCKS.any { rock ->
                rock.id == comment.rockId && SECTORS.any {
                    it.id == rock.parentId && it.parentId == regionId
                }
            }
        }
        assertTrue(equal(rockComments, _rockCommentDao.getAllInRegion(regionId)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_invalidCountryName_returnsEmptyList() = runTest {
        assertTrue(_rockCommentDao.getAllInCountry(INVALID_NAME).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_noRocksAvailable_returnsEmptyList() = runTest {
        assertTrue(_rockCommentDao.getAllInCountry(COUNTRIES.last().name).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAllInCountry_rocksAvailable_returnsCorrespondingRockComments() = runTest {
        val countryName = COUNTRIES.first().name
        val rockComments = ROCK_COMMENTS.filter { comment ->
            ROCKS.any { rock ->
                rock.id == comment.rockId && SECTORS.any { sector ->
                    sector.id == rock.parentId && REGIONS.any {
                        it.id == sector.parentId && it.country == countryName
                    }
                }
            }
        }
        assertTrue(equal(rockComments, _rockCommentDao.getAllInCountry(countryName)))
    }
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RockCommentDaoDeletionTests {

    private lateinit var _db: AppDatabase
    private lateinit var _rockCommentDao: RockCommentDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _rockCommentDao = _db.rockCommentDao()
    }

    @BeforeEach
    fun init() {
        TestDB.initRockComments(_rockCommentDao)
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
    fun deleteAll_rockCommentTableBecomesEmpty() = runTest {
        _rockCommentDao.deleteAll()
        assertTrue(_rockCommentDao.all.isEmpty())
    }
}
