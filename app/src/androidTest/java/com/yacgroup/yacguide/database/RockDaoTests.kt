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
import com.yacgroup.yacguide.database.TestDB.Companion.ROCKS
import com.yacgroup.yacguide.database.TestDB.Companion.ROCK_COMMENTS
import com.yacgroup.yacguide.database.TestDB.Companion.SECTORS
import com.yacgroup.yacguide.database.comment.RockComment
import com.yacgroup.yacguide.equal
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RockDaoTests {

    private lateinit var _db: AppDatabase
    private lateinit var _rockDao: RockDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _rockDao = _db.rockDao()

        TestDB.initCountries(_db.countryDao())
        TestDB.initRegions(_db.regionDao())
        TestDB.initSectors(_db.sectorDao())
        TestDB.initRocks(_rockDao)
        TestDB.initRockComments(_db.rockCommentDao())
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @Test
    fun getAllByName_nameNotAvailable_returnsEmptyList() = runBlockingTest {
        assertTrue(_rockDao.getAllByName(INVALID_NAME).isEmpty())
    }

    @Test
    fun getAllByName_nameAvailable_returnsCorrespondingRocks() = runBlockingTest {
        val rock = ROCKS.first()
        assertTrue(equal(listOf(rock), _rockDao.getAllByName(rock.name!!)))
    }

    @Test
    fun getAllByRelevance_rocksForEveryRelevanceRequested_returnsCorrespondingRocksRespectively() = runBlockingTest {
        RockComment.RELEVANCE_MAP.keys.forEach { relevance ->
            val rocks = ROCKS.filter { rock ->
                val comments = ROCK_COMMENTS.filter {
                    it.rockId == rock.id
                }
                comments.isNotEmpty() && relevance >= comments.sumOf {
                    it.qualityId.toDouble() / comments.size
                }
            }
            assertTrue(equal(rocks, _rockDao.getAllByRelevance(relevance)))
        }
    }

    @Test
    fun getAllInCountry_invalidCountryName_returnsEmptyList() = runBlockingTest {
        assertTrue(_rockDao.getAllInCountry(INVALID_NAME).isEmpty())
    }

    @Test
    fun getAllInCountry_noRocksAvailable_returnsEmptyList() = runBlockingTest {
        assertTrue(_rockDao.getAllInCountry(COUNTRIES.last().name).isEmpty())
    }

    @Test
    fun getAllInCountry_rocksAvailable_returnsCorrespondingRocks() = runBlockingTest {
        val country1Name = COUNTRIES.first().name
        val rocksInCountry1 = ROCKS.filter { rock ->
            SECTORS.any { sector ->
                sector.id == rock.parentId && REGIONS.any {
                    it.id == sector.parentId && it.country == country1Name
                }
            }
        }
        assertTrue(equal(rocksInCountry1, _rockDao.getAllInCountry(country1Name)))
    }

    @Test
    fun getAllInRegion_invalidRegionId_returnsEmptyList() = runBlockingTest {
        assertTrue(_rockDao.getAllInRegion(INVALID_ID).isEmpty())
    }

    @Test
    fun getAllInRegion_noRocksAvailable_returnsEmptyList() = runBlockingTest {
        assertTrue(_rockDao.getAllInRegion(REGIONS.last().id).isEmpty())
    }

    @Test
    fun getAllInRegion_rocksAvailable_returnsCorrespondingRocks() = runBlockingTest {
        val region1Id = REGIONS.first().id
        val rocksInRegion1 = ROCKS.filter { rock ->
            SECTORS.any { it.id == rock.parentId && it.parentId == region1Id }
        }
        assertTrue(equal(rocksInRegion1, _rockDao.getAllInRegion(region1Id)))
    }

    @Test
    fun getAllInSector_invalidSectorId_returnsEmptyList() = runBlockingTest {
        assertTrue(_rockDao.getAllInSector(INVALID_ID).isEmpty())
    }

    @Test
    fun getAllInSector_noRocksAvailable_returnsEmptyList() = runBlockingTest {
        assertTrue(_rockDao.getAllInSector(SECTORS.last().id).isEmpty())
    }

    @Test
    fun getAllInSector_rocksAvailable_returnsCorrespondingRocks() = runBlockingTest {
        val sector1Id = SECTORS.first().id
        val rocksInSector1 = ROCKS.filter {
            it.parentId == sector1Id
        }
        assertTrue(equal(rocksInSector1, _rockDao.getAllInSector(sector1Id)))
    }

    @Test
    fun getAllByNameInSector_nameAvailableInDifferentSector_returnsEmptyList() = runBlockingTest {
        assertTrue(_rockDao.getAllByNameInSector(SECTORS.first().id, ROCKS.last().name!!).isEmpty())
    }

    @Test
    fun getRock_invalidRockId_returnsNull() = runBlockingTest {
        assertNull(_rockDao.getRock(INVALID_ID))
    }

    @Test
    fun getRock_rockAvailable_returnsRock() = runBlockingTest {
        val rock = ROCKS.first()
        assertTrue(_rockDao.getRock(rock.id) == rock)
    }
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RockDaoDeletionTests {

    private lateinit var _db: AppDatabase
    private lateinit var _rockDao: RockDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _rockDao = _db.rockDao()

        TestDB.initRocks(_rockDao)
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @Test
    fun deleteAll_rockTableBecomesEmpty() = runBlockingTest {
        _rockDao.deleteAll()
        assertTrue(_rockDao.all.isEmpty())
    }
}