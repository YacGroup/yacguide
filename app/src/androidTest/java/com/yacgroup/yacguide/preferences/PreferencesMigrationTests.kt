/*
 * Copyright (C) 2024 Christian Sommer
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

package com.yacgroup.yacguide.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.yacgroup.yacguide.migration.PreferencesMigration
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PreferencesMigrationTests {

    private lateinit var _context: Context
    private lateinit var _sharedPreferences: SharedPreferences
    private lateinit var _preferenceMigration: PreferencesMigration
    private lateinit var _preferencesTestData: Map<String, Triple<String, PreferencesType, Any>>

    /**
     * Create preference of version V1 with random values.
     *
     * @param testFull Controls whether a full preference set is created or not.
     *
     * @return List of keys created.
     */
    private fun createV1(testFull: Boolean = true) : ArrayList<String> {
        return ArrayList<String>().apply {
            _sharedPreferences.apply {
                this.edit().apply {
                    for ((keyV1, tripleValue) in _preferencesTestData) {
                        val keyType = tripleValue.second
                        val value = tripleValue.third
                        if (!testFull && Random.nextBoolean()) {
                            continue
                        }
                        add(keyV1)
                        when (keyType) {
                            PreferencesType.eBoolean -> putBoolean(keyV1, value as Boolean)
                            PreferencesType.eInt -> putInt(keyV1, value as Int)
                        }
                    }
                }.apply()
            }
        }
    }

    private fun deleteSharedPreferences() = _sharedPreferences.edit().clear().apply()

    /**
     * Check that the keys get migrated from V1 to V2 and that the values are preserved.
     *
     * @param keysV1 List of V1 keys to be checked.
     */
    private fun checkV2(keysV1: ArrayList<String>) {
        assertTrue(_sharedPreferences.getInt("preference_version", 1) == 2)
        keysV1.forEach { keyV1 ->
            val keyV2 = _preferencesTestData[keyV1]?.first
            val keyType = _preferencesTestData[keyV1]?.second
            val value = _preferencesTestData[keyV1]?.third
            when (keyType) {
                PreferencesType.eBoolean -> {
                    assertTrue(_sharedPreferences.getBoolean(keyV2, (value as Boolean).not()) == value)
                }
                PreferencesType.eInt -> {
                    assertTrue(_sharedPreferences.getInt(keyV2, (value as Int).inv()) == value)
                }
                else -> {
                    assertTrue(false)
                }
            }
            assertFalse(_sharedPreferences.contains(keyV1))
        }
    }

    @BeforeAll
    fun setup() {
        _context = ApplicationProvider.getApplicationContext()
        _sharedPreferences = _context.getSharedPreferences(
            "test_migration_v1_to_v2",
            Context.MODE_PRIVATE
        )
        _preferenceMigration = PreferencesMigration(
            context = _context,
            customSettings = _sharedPreferences
        )
        _preferencesTestData = PreferencesFormatV2.entries.associate {
            _context.getString(it.resIdKeyV1) to Triple(
                _context.getString(it.resIdKey),
                it.keyType,
                when (it.keyType) {
                    PreferencesType.eBoolean -> Random.nextBoolean()
                    PreferencesType.eInt -> Random.nextInt()
                }
            )
        }
    }

    @AfterAll
    fun tearDown() {
        deleteSharedPreferences()
    }

    @Test
    fun from_v1_v2_full() {
        deleteSharedPreferences()
        val v1Keys = createV1(testFull = true)
        _preferenceMigration.migrateFromVersion1To2()
        checkV2(v1Keys)
    }

    @Test
    fun from_v1_v2_partial() {
        deleteSharedPreferences()
        val v1Keys = createV1(testFull = false)
        _preferenceMigration.migrateFromVersion1To2()
        checkV2(v1Keys)
    }
}
