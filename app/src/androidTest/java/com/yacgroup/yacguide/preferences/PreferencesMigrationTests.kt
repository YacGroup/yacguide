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
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.migration.PreferencesFormatV2
import com.yacgroup.yacguide.migration.PreferencesMigration
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import kotlin.random.Random
import kotlin.reflect.full.createType

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PreferencesMigrationTests {

    companion object {
        private val V1_TO_V2_MAP = mapOf(
            R.string.order_tourbook_chronologically to
                    Pair(R.string.pref_key_order_tourbook_chronologically, Random.nextBoolean()),
            R.string.only_official_summits to
                    Pair(R.string.pref_key_only_official_summits, Random.nextBoolean()),
            R.string.only_official_routes to
                    Pair(R.string.pref_key_only_official_routes, Random.nextBoolean()),
            R.string.count_summits to
                    Pair(R.string.pref_key_count_summits, Random.nextBoolean()),
            R.string.count_massifs to
                    Pair(R.string.pref_key_count_massifs, Random.nextBoolean()),
            R.string.count_boulders to
                    Pair(R.string.pref_key_count_boulders, Random.nextBoolean()),
            R.string.count_caves to
                    Pair(R.string.pref_key_count_caves, Random.nextBoolean()),
            R.string.count_unofficial_rocks to
                    Pair(R.string.pref_key_count_unofficial_rocks, Random.nextBoolean()),
            R.string.count_prohibited_rocks to
                    Pair(R.string.pref_key_count_prohibited_rocks, Random.nextBoolean()),
            R.string.count_collapsed_rocks to
                    Pair(R.string.pref_key_count_collapsed_rocks, Random.nextBoolean()),
            R.string.count_only_leads to
                    Pair(R.string.pref_key_count_only_leads, Random.nextBoolean()),
            R.string.colorize_tourbook_entries to
                    Pair(R.string.pref_key_colorize_tourbook_entries, Random.nextBoolean()),
            R.string.lead to Pair(R.string.pref_key_color_lead, Random.nextInt()),
            R.string.follow to Pair(R.string.pref_key_color_follow, Random.nextInt())

        ).entries.associate {
            ApplicationProvider.getApplicationContext<Context>().getString(it.key) to
            Pair(
                ApplicationProvider.getApplicationContext<Context>().getString(it.value.first),
                it.value.second
            )
        }
    }
    private lateinit var _context: Context
    private lateinit var _sharedPreferences: SharedPreferences
    private lateinit var _preferenceMigration: PreferencesMigration

    /**
     * Create preference of version V1 with random values.
     *
     * @param testFull Controls whether a full preference set is created or not.
     *
     * @return List of key created.
     */
    private fun createV1(testFull: Boolean = true) : ArrayList<String> {
        return ArrayList<String>().apply {
            _sharedPreferences.apply {
                this.edit().apply {
                    for ((keyV1, keyV2AndPropValue) in V1_TO_V2_MAP) {
                        val propValue = keyV2AndPropValue.second
                        val keyType = propValue::class.createType()
                        if (!testFull && Random.nextBoolean()) {
                            continue
                        }
                        add(keyV1)
                        if (keyType == Boolean::class.createType()) {
                            putBoolean(keyV1, propValue as Boolean)
                        } else if (keyType == Int::class.createType()) {
                            putInt(keyV1, propValue as Int)
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
            val keyV2 = V1_TO_V2_MAP[keyV1]?.first
            val value = V1_TO_V2_MAP[keyV1]?.second
            when (value!!::class.createType()) {
                Boolean::class.createType() -> {
                    assertTrue(_sharedPreferences.getBoolean(keyV2, (value as Boolean).not()) == value)
                }
                Int::class.createType() -> {
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
