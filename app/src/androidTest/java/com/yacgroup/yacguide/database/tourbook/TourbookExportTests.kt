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
import com.yacgroup.yacguide.database.tourbook.TourbookTestFixture.Companion.ASCEND
import com.yacgroup.yacguide.database.tourbook.TourbookTestFixture.Companion.ORPHAN_ASCEND
import kotlinx.coroutines.test.runTest
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.json.JSONArray
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TourbookEntryVerboseTests {

    private lateinit var _db: DatabaseWrapper

    @BeforeAll
    fun setup() {
        _db = TourbookTestFixture.setUpDb(ApplicationProvider.getApplicationContext())
    }

    @AfterAll
    fun teardown() {
        TourbookTestFixture.cleanDb(_db)
    }

    @Test
    fun keys_returnsFixedFieldNamesInValuesOrder() {
        assertEquals(
            listOf(
                "country", "regionName", "sectorFirstName", "sectorSecondName",
                "rockFirstName", "rockSecondName", "routeFirstName", "routeSecondName",
                "routeGrade", "notes", "date", "style", "partners"
            ),
            TourbookEntryVerbose.keys()
        )
    }

    @Test
    fun values_fullHierarchyAvailable_returnsResolvedFields() {
        val entry = TourbookEntryVerbose(ASCEND, _db)

        assertEquals(
            listOf(
                "Country1", "Region1", "Sector1First", "Sector1Second",
                "Rock1First", "Rock1Second", "Route1First", "Route1Second",
                "7a", "Great day", "15.06.2020", "Onsight", "Partner1,Partner2"
            ),
            entry.values()
        )
    }

    @Test
    fun asMap_fullHierarchyAvailable_mapsKeysToValues() {
        val entry = TourbookEntryVerbose(ASCEND, _db)
        assertEquals(TourbookEntryVerbose.keys().zip(entry.values()).toMap(), entry.asMap())
    }

    @Test
    fun values_routeAndAncestorsMissing_returnsEmptyStringsForResolvedFields() {
        val entry = TourbookEntryVerbose(ORPHAN_ASCEND, _db)

        assertEquals(
            listOf(
                "", "", "", "",
                "", "", "", "",
                "", "", "02.01.2021", "Nachstieg", ""
            ),
            entry.values()
        )
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JsonExporterTests {

    private lateinit var _context: Context
    private lateinit var _db: DatabaseWrapper

    @BeforeAll
    fun setup() {
        _context = ApplicationProvider.getApplicationContext()
        _db = TourbookTestFixture.setUpDb(_context)
    }

    @AfterAll
    fun teardown() {
        TourbookTestFixture.cleanDb(_db)
    }

    private fun exportAndParse(): JSONArray {
        val file = File(_context.cacheDir, "tourbook_export_test.json")
        val uri = Uri.fromFile(file)
        runTest {
            JsonExporter(_context.contentResolver, _db).export(uri)
        }
        return JSONArray(file.readText(Charsets.UTF_8))
    }

    @Test
    fun export_writesOneEntryPerAscend() {
        assertEquals(_db.getAscends().size, exportAndParse().length())
    }

    @Test
    fun export_fullAscend_writesRawIdsAndPartnerNames() {
        val jsonAscends = exportAndParse()
        val entry = (0 until jsonAscends.length())
            .map { jsonAscends.getJSONObject(it) }
            .first { it.getString(TourbookExchangeKeys.eROUTE.key) == ASCEND.routeId.toString() }

        assertEquals(ASCEND.styleId.toString(), entry.getString(TourbookExchangeKeys.eSTYLE.key))
        assertEquals(ASCEND.year.toString(), entry.getString(TourbookExchangeKeys.eYEAR.key))
        assertEquals(ASCEND.month.toString(), entry.getString(TourbookExchangeKeys.eMONTH.key))
        assertEquals(ASCEND.day.toString(), entry.getString(TourbookExchangeKeys.eDAY.key))
        assertEquals("Great day", entry.getString(TourbookExchangeKeys.eNOTES.key))

        val partners = entry.getJSONArray(TourbookExchangeKeys.ePARTNERS.key)
        assertEquals(listOf("Partner1", "Partner2"), (0 until partners.length()).map { partners.getString(it) })
    }

    @Test
    fun export_ascendWithoutNotesOrPartners_omitsNotesAndWritesEmptyPartnerList() {
        val jsonAscends = exportAndParse()
        val entry = (0 until jsonAscends.length())
            .map { jsonAscends.getJSONObject(it) }
            .first { it.getString(TourbookExchangeKeys.eROUTE.key) == ORPHAN_ASCEND.routeId.toString() }

        assertFalse(entry.has(TourbookExchangeKeys.eNOTES.key))
        assertEquals(0, entry.getJSONArray(TourbookExchangeKeys.ePARTNERS.key).length())
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JsonVerboseExporterTests {

    private lateinit var _context: Context
    private lateinit var _db: DatabaseWrapper

    @BeforeAll
    fun setup() {
        _context = ApplicationProvider.getApplicationContext()
        _db = TourbookTestFixture.setUpDb(_context)
    }

    @AfterAll
    fun teardown() {
        TourbookTestFixture.cleanDb(_db)
    }

    private fun exportAndParse(): JSONArray {
        val file = File(_context.cacheDir, "tourbook_export_verbose_test.json")
        val uri = Uri.fromFile(file)
        runTest {
            JsonVerboseExporter(_context.contentResolver, _db).export(uri)
        }
        return JSONArray(file.readText(Charsets.UTF_8))
    }

    @Test
    fun export_writesOneEntryPerAscendUsingVerboseKeys() {
        val jsonAscends = exportAndParse()
        assertEquals(_db.getAscends().size, jsonAscends.length())

        for (i in 0 until jsonAscends.length()) {
            val entry = jsonAscends.getJSONObject(i)
            assertEquals(TourbookEntryVerbose.keys().toSet(), entry.keys().asSequence().toSet())
        }
    }

    @Test
    fun export_fullAscend_matchesTourbookEntryVerboseValues() {
        val expected = TourbookEntryVerbose(ASCEND, _db)
        val jsonAscends = exportAndParse()
        val entry = (0 until jsonAscends.length())
            .map { jsonAscends.getJSONObject(it) }
            .first { it.getString("routeFirstName") == "Route1First" }

        TourbookEntryVerbose.keys().forEach { key ->
            assertEquals(expected.asMap()[key], entry.getString(key), "mismatch for key '$key'")
        }
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CsvExporterTests {

    private lateinit var _context: Context
    private lateinit var _db: DatabaseWrapper

    @BeforeAll
    fun setup() {
        _context = ApplicationProvider.getApplicationContext()
        _db = TourbookTestFixture.setUpDb(_context)
    }

    @AfterAll
    fun teardown() {
        TourbookTestFixture.cleanDb(_db)
    }

    private fun exportAndParse(): CSVParser {
        val file = File(_context.cacheDir, "tourbook_export_test.csv")
        val uri = Uri.fromFile(file)
        runTest {
            CsvExporter(_context.contentResolver, _db).export(uri)
        }
        val format = CSVFormat.Builder.create(CSVFormat.DEFAULT).apply {
            setHeader()
            setSkipHeaderRecord(true)
        }.get()
        return CSVParser.parse(file.readText(Charsets.UTF_8), format)
    }

    @Test
    fun export_writesHeaderMatchingTourbookEntryVerboseKeys() {
        assertEquals(TourbookEntryVerbose.keys(), exportAndParse().headerNames)
    }

    @Test
    fun export_writesOneRecordPerAscend() {
        assertEquals(_db.getAscends().size, exportAndParse().records.size)
    }

    @Test
    fun export_fullAscend_matchesTourbookEntryVerboseValues() {
        val expected = TourbookEntryVerbose(ASCEND, _db).asMap()
        val record = exportAndParse().records.first { it.get("routeFirstName") == "Route1First" }

        expected.forEach { (key, value) -> assertEquals(value, record.get(key), "mismatch for key '$key'") }
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TourbookExporterFactoryTests {

    private lateinit var _factory: TourbookExporterFactory

    @BeforeAll
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _factory = TourbookExporterFactory(DatabaseWrapper(context), context.contentResolver)
    }

    @Test
    fun create_json_returnsPlainJsonExporter() {
        assertEquals(JsonExporter::class, _factory.create(TourbookExportFormat.eJSON)::class)
    }

    @Test
    fun create_jsonVerbose_returnsJsonVerboseExporter() {
        assertTrue(_factory.create(TourbookExportFormat.eJSONVERBOSE) is JsonVerboseExporter)
    }

    @Test
    fun create_csv_returnsCsvExporter() {
        assertTrue(_factory.create(TourbookExportFormat.eCSV) is CsvExporter)
    }
}
