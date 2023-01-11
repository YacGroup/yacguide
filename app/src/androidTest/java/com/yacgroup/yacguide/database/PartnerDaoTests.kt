/*
 * Copyright (C) 2022, 2023 Axel Paetzold
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
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_ID
import com.yacgroup.yacguide.database.TestDB.Companion.INVALID_NAME
import com.yacgroup.yacguide.database.TestDB.Companion.PARTNERS
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PartnerDaoTests {

    private lateinit var _db: AppDatabase
    private lateinit var _partnerDao: PartnerDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _partnerDao = _db.partnerDao()

        TestDB.initPartners(_partnerDao)
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getId_invalidName_returnsZero() = runTest {
        assertEquals(0, _partnerDao.getId(INVALID_NAME))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getId_nameIsAvailable_returnsCorrespondingId() = runTest {
        val partner = PARTNERS.first()
        assertEquals(partner.id, _partnerDao.getId(partner.name!!))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getPartner_invalidId_returnsNull() = runTest {
        assertNull(_partnerDao.getPartner(INVALID_ID))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getPartner_idIsAvailable_returnsCorrespondingPartner() = runTest {
        val partner = PARTNERS.first()
        assertEquals(partner, _partnerDao.getPartner(partner.id))
    }
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PartnerDaoDeletionTests {
    private lateinit var _db: AppDatabase
    private lateinit var _partnerDao: PartnerDao

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        _partnerDao = _db.partnerDao()
    }

    @AfterAll
    fun teardown() {
        _db.close()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAll_partnerTableBecomesEmpty() = runTest {
        TestDB.initPartners(_partnerDao)

        _partnerDao.deleteAll()
        assertTrue(_partnerDao.all.isEmpty())
    }
}