/*
 * Copyright (C) 2026 Christian Sommer
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

package com.yacgroup.yacguide.database.tourbook

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.yacgroup.yacguide.database.DatabaseWrapper
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JsonImporterTests {

    private lateinit var _context: Context
    private lateinit var _db: DatabaseWrapper

    @BeforeAll
    fun setup() {
        _context = ApplicationProvider.getApplicationContext()
        _db = DatabaseWrapper(_context)
        _cleanDb()
    }

    @AfterEach
    fun cleanupAfterEach() {
        _cleanDb()
    }

    @AfterAll
    fun teardown() {
        _cleanDb()
    }

    private fun _cleanDb() {
        _db.deleteAscends()
        _db.deletePartners()
    }

    private fun _ascendJson(
        routeId: Int = 1,
        styleId: Int = 0,
        year: Int = 2020,
        month: Int = 6,
        day: Int = 15,
        partners: List<String> = emptyList(),
        notes: String = ""
    ): JSONObject = JSONObject().apply {
        put(TourbookExchangeKeys.eROUTE.key, routeId.toString())
        put(TourbookExchangeKeys.eSTYLE.key, styleId.toString())
        put(TourbookExchangeKeys.eYEAR.key, year.toString())
        put(TourbookExchangeKeys.eMONTH.key, month.toString())
        put(TourbookExchangeKeys.eDAY.key, day.toString())
        put(TourbookExchangeKeys.ePARTNERS.key, JSONArray().apply { partners.forEach { put(it) } })
        put(TourbookExchangeKeys.eNOTES.key, notes)
    }

    private fun _import(jsonAscends: JSONArray) {
        val file = File(_context.cacheDir, "tourbook_import_test.json")
        file.writeText(jsonAscends.toString(), Charsets.UTF_8)
        val uri = Uri.fromFile(file)
        runTest {
            JsonImporter(_db, _context.contentResolver).import(uri)
        }
    }

    @Test
    fun import_singleFullEntry_writesRawIdsNotesAndPartners() {
        _import(JSONArray().apply {
            put(_ascendJson(
                routeId = 42, styleId = 1, year = 2020, month = 6, day = 15,
                partners = listOf("Partner1", "Partner2"), notes = "Great day"
            ))
        })

        val ascends = _db.getAscends()
        assertEquals(1, ascends.size)
        val ascend = ascends[0]
        assertEquals(42, ascend.routeId)
        assertEquals(1, ascend.styleId)
        assertEquals(2020, ascend.year)
        assertEquals(6, ascend.month)
        assertEquals(15, ascend.day)
        assertEquals("Great day", ascend.notes)
        assertEquals(
            listOf("Partner1", "Partner2"),
            _db.getPartnerNames(ascend.partnerIds.orEmpty())
        )
    }

    @Test
    fun import_entryWithoutPartners_writesEmptyPartnerList() {
        _import(JSONArray().apply {
            put(_ascendJson(partners = emptyList()))
        })

        val ascend = _db.getAscends().first()
        assertTrue(ascend.partnerIds.orEmpty().isEmpty())
    }

    @Test
    fun import_samePartnerNameReferencedByMultipleEntries_reusesSinglePartnerRecord() {
        _import(JSONArray().apply {
            put(_ascendJson(routeId = 1, partners = listOf("Partner1")))
            put(_ascendJson(routeId = 2, partners = listOf(" Partner1 ")))
        })

        assertEquals(1, _db.getPartners().size)
        val ascends = _db.getAscends()
        val partnerIds = ascends.map { it.partnerIds.orEmpty().single() }.toSet()
        assertEquals(1, partnerIds.size)
        assertEquals("Partner1", _db.getPartner(partnerIds.single())?.name)
    }

    @Test
    fun import_replacesPreviouslyImportedAscendsAndPartners() {
        _import(JSONArray().apply {
            put(_ascendJson(routeId = 1, partners = listOf("OldPartner")))
        })
        assertEquals(1, _db.getAscends().size)
        assertEquals(1, _db.getPartners().size)

        _import(JSONArray().apply {
            put(_ascendJson(routeId = 2, partners = listOf("NewPartner")))
        })

        val ascends = _db.getAscends()
        assertEquals(1, ascends.size)
        assertEquals(2, ascends[0].routeId)
        val partners = _db.getPartners()
        assertEquals(1, partners.size)
        assertEquals("NewPartner", partners[0].name)
    }

    @Test
    fun import_missingNotesField_throwsJsonException() {
        val jsonAscends = JSONArray().apply {
            put(_ascendJson().apply { remove(TourbookExchangeKeys.eNOTES.key) })
        }

        assertThrows(JSONException::class.java) { _import(jsonAscends) }
    }
}
