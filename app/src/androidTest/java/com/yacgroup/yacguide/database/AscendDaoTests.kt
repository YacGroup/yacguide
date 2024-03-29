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
import com.yacgroup.yacguide.database.TestDB.Companion.ASCENDS
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_ID
import com.yacgroup.yacguide.database.TestDB.Companion.ROCKS
import com.yacgroup.yacguide.database.TestDB.Companion.ROUTES
import com.yacgroup.yacguide.equal
import com.yacgroup.yacguide.utils.AscendStyle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AscendDaoTests {

    private lateinit var _db: AppDatabase
    private lateinit var _ascendDao: AscendDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _ascendDao = _db.ascendDao()

        TestDB.initRoutes(_db.routeDao())
        TestDB.initAscends(_ascendDao)
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsBelowStyleId_returnsCorrespondingAscendsOrderedByDate() = runTest {
        val refAscend = ASCENDS.last()
        val ascends = ASCENDS.filter { it.styleId < refAscend.styleId }
        assertTrue(equal(ascends.sortedBy { "${it.year}-${it.month}-${it.day}" }, _ascendDao.getAscendsBelowStyleId(refAscend.styleId)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsForYearAndStyle_invalidYear_returnsEmptyList() = runTest {
        assertTrue(_ascendDao.getAscendsForYearAndStyle(INVALID_ID, ASCENDS.first().styleId).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsForYearAndStyle_invalidStyleId_returnsEmptyList() = runTest {
        assertTrue(_ascendDao.getAscendsForYearAndStyle(ASCENDS.first().year, INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsForYearAndStyle_yearAndStyleIdAvailable_returnsCorrespondingAscends() = runTest {
        val refAscend = ASCENDS.last()
        val ascends = ASCENDS.filter {
            it.year == refAscend.year && it.styleId == refAscend.styleId
        }
        assertTrue(equal(ascends, _ascendDao.getAscendsForYearAndStyle(refAscend.year, refAscend.styleId)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsForYearBelowStyleId_invalidYear_returnsEmptyList() = runTest {
        assertTrue(_ascendDao.getAscendsForYearBelowStyleId(INVALID_ID, AscendStyle.ePROJECT.id).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsForYearBelowStyleId_yearAvailable_returnsCorrespondingAscendsOrderedByDate() = runTest {
        val refAscend = ASCENDS.last()
        val ascends = ASCENDS.filter {
            it.year == refAscend.year && it.styleId < refAscend.styleId
        }
        assertTrue(equal(ascends.sortedBy { "${it.year}-${it.month}-${it.day}" }, _ascendDao.getAscendsForYearBelowStyleId(refAscend.year, refAscend.styleId)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsForRoute_invalidRouteId_returnsEmptyList() = runTest {
        assertTrue(_ascendDao.getAscendsForRoute(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsForRoute_noAscendsAvailable_returnsEmptyList() = runTest {
        assertTrue(_ascendDao.getAscendsForRoute(ROUTES.last().id).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsForRoute_ascendsAvailable_returnsCorrespondingAscendsOrderedByDate() = runTest {
        val route = ROUTES.first()
        val ascends = ASCENDS.filter {
            it.routeId == route.id
        }
        assertTrue(equal(ascends.sortedBy { "${it.year}-${it.month}-${it.day}" }, _ascendDao.getAscendsForRoute(route.id)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsForRock_invalidRockId_returnsEmptyList() = runTest {
        assertTrue(_ascendDao.getAscendsForRock(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsForRock_noAscendsAvailable_returnsEmptyList() = runTest {
        assertTrue(_ascendDao.getAscendsForRock(ROCKS.last().id).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendsForRock_ascendsAvailable_returnsCorrespondingAscends() = runTest {
        val rock = ROCKS.first()
        val ascends = ASCENDS.filter { ascend ->
            ROUTES.filter {
                it.parentId == rock.id
            }.any {
                it.id == ascend.routeId
            }
        }
        assertTrue(equal(ascends, _ascendDao.getAscendsForRock(rock.id)))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscend_invalidAscendId_returnsNull() = runTest {
        assertNull(_ascendDao.getAscend(INVALID_ID))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscend_ascendAvailable_returnsAscend() = runTest {
        val ascend = ASCENDS.first()
        assertEquals(ascend, _ascendDao.getAscend(ascend.id))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getYears_invalidStyleId_returnsEmptyArray() = runTest {
        assertTrue(_ascendDao.getYears(INVALID_ID).isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getYears_yearsAvailable_returnsCorrespondingYears() = runTest {
        val styleId = ASCENDS.last().styleId
        val years = ASCENDS.filter {
            it.styleId == styleId
        }.map {
            it.year
        }
        assertTrue(equal(years, _ascendDao.getYears(styleId).toList()))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getYearsBelowStyleId_yearsAvailable_returnsCorrespondingYears() = runTest {
        val styleId = ASCENDS.last().styleId
        val years = ASCENDS.filter {
            it.styleId < styleId
        }.map {
            it.year
        }
        assertTrue(equal(years, _ascendDao.getYearsBelowStyleId(styleId).toList()))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendCountsPerYear_invalidStyleIdRange_returnsYearCountsOfZero() = runTest {
        _ascendDao.getAscendCountPerYear(5, 2).forEach { assertEquals(0, it.count) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendCountsPerYear_validStyleIdRange_returnsListWithAccordingStyleCountsPerYear() = runTest {
        val leadAscends = ASCENDS.filter { ascend ->
            ascend.styleId in (AscendStyle.eONSIGHT.id..AscendStyle.eHOCHGESCHLEUDERT.id)
        }
        _ascendDao.getAscendCountPerYear(AscendStyle.eONSIGHT.id, AscendStyle.eHOCHGESCHLEUDERT.id).forEach { leadCount ->
            assertEquals(leadAscends.filter { it.year == leadCount.year }.size, leadCount.count)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getAscendCountsPerYear_callMultipleTimes_returnsListsOfEqualLengthAndYearOrder() = runTest {
        val followCounts = _ascendDao.getAscendCountPerYear(AscendStyle.eALTERNATINGLEADS.id, AscendStyle.eHINTERHERGEHAMPELT.id)
        val soloCounts = _ascendDao.getAscendCountPerYear(AscendStyle.eSOLO.id, AscendStyle.eSOLO.id)

        assertEquals(followCounts.size, soloCounts.size)
        followCounts.forEachIndexed { idx, followCount ->
            assertEquals(followCount.year, soloCounts[idx].year)
        }
    }
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AscendDaoDeletionTests {

    private lateinit var _db: AppDatabase
    private lateinit var _ascendDao: AscendDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _ascendDao = _db.ascendDao()

        TestDB.initAscends(_ascendDao)
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAll_routeTableBecomesEmpty() = runTest {
        _ascendDao.deleteAll()
        assertTrue(_ascendDao.all.isEmpty())
    }
}