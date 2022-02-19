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
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.*

import org.junit.jupiter.api.Assertions.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CountryDaoTests {

    private lateinit var _db: AppDatabase
    private lateinit var _countryDao: CountryDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _countryDao = _db.countryDao()
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
    fun all_countriesUnordered_returnsOrderedList() = runBlockingTest {
        _countryDao.insert(TestDB.COUNTRIES.reversed())

        assertTrue(equal(TestDB.COUNTRIES, _countryDao.all))
    }

    @Test
    fun getAllNonEmpty_noRegionsAvailable_returnsEmptyList() = runBlockingTest {
        TestDB.initCountries(_countryDao)

        assertTrue(_countryDao.getAllNonEmpty().isEmpty())
    }

    @Test
    fun getAllNonEmpty_regionsWithParentCountriesAvailable_returnsCorrespondingCountries() = runBlockingTest {
        TestDB.initCountries(_countryDao)
        TestDB.initRegions(_db.regionDao())
        val nonEmptyCountries = TestDB.COUNTRIES.filter { countryIt ->
            TestDB.REGIONS.any { it.country == countryIt.name }
        }

        assertTrue(equal(nonEmptyCountries, _countryDao.getAllNonEmpty()))
    }

    @Test
    fun deleteAll_countryCountBecomesZero() = runBlockingTest {
        TestDB.initCountries(_countryDao)

        _countryDao.deleteAll()
        assertTrue(_countryDao.all.isEmpty())
    }
}
