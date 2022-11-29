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

package com.yacgroup.yacguide.list_adapters

import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.utils.AscendStyle
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RockCounterTest {

    private lateinit var _ledSummits: List<Rock>
    private lateinit var _ascendedSummits: List<Rock>
    private lateinit var _ascendedRocks: List<Rock>
    private lateinit var _permittedRocks: List<Rock>
    private lateinit var _allRocks: List<Rock>

    @BeforeAll
    fun setup() {
        _ledSummits = listOf(
            Rock(1, 1f, Rock.typeSummit, ' ', "Rock1", 0f, 0f, AscendStyle.bitMask(AscendStyle.eALLFREE.id), 1),
            Rock(2, 2f, Rock.typeAlpine, ' ', "Rock2", 0f, 0f, AscendStyle.bitMask(AscendStyle.eSOLO.id), 1)
        )
        _ascendedSummits = _ledSummits + listOf(
            Rock(3, 3f, Rock.typeSummit, ' ', "Rock3", 0f, 0f, AscendStyle.bitMask(AscendStyle.eFOLLOWED.id), 1),
            Rock(4, 4f, Rock.typeAlpine, ' ', "Rock4", 0f, 0f, AscendStyle.bitMask(AscendStyle.eHINTERHERGEHAMPELT.id), 1)
        )
        _ascendedRocks = _ascendedSummits + listOf(
            Rock(5, 5f, Rock.typeMassif, ' ', "Rock5", 0f, 0f, AscendStyle.bitMask(AscendStyle.eALTERNATINGLEADS.id), 1),
            Rock(6, 6f, Rock.typeCave, ' ', "Rock6", 0f, 0f, AscendStyle.bitMask(AscendStyle.eSOLO.id), 1)
        )
        _permittedRocks = _ascendedRocks + listOf(
            Rock(7, 7f, Rock.typeBoulder, ' ', "Rock7", 0f, 0f, AscendStyle.bitMask(AscendStyle.eBOTCHED.id), 1),
            Rock(8, 8f, Rock.typeStonePit, ' ', "Rock8", 0f, 0f, 0, 1)
        )
        _allRocks = _permittedRocks + listOf(
            Rock(9, 9f, Rock.typeSummit, 'X', "Rock9", 0f, 0f, 0, 1),
            Rock(10, 10f, Rock.typeUnofficial, 'E', "Rock10", 0f, 0f, 0, 1)
        )
    }

    @Test
    fun isApplicable_noCountersConfigured_returnsFalse() {
        val counter = RockCounter(RockCounterConfig())
        assertFalse(counter.isApplicable())
    }

    @Test
    fun isApplicable_atLeastOneCounterConfigured_returnsTrue() {
        val counter = RockCounter(RockCounterConfig(countSummits = true))
        assertTrue(counter.isApplicable())
    }

    @Test
    fun calculateRockCount_noCountersConfigured_rockCountIsZero() {
        val counter = RockCounter(RockCounterConfig())
        val rockCount = counter.calculateRockCount(_allRocks)
        assertEquals(0, rockCount.ascended)
        assertEquals(0, rockCount.total)
    }

    @Test
    fun calculateRockCount_allCountersConfigured_countsOnlyAscendedRocks() {
        val counter = RockCounter(RockCounterConfig(countSummits = true,
                                                    countMassifs = true,
                                                    countBoulders = true,
                                                    countCaves = true,
                                                    countUnofficialRocks = true,
                                                    countProhibitedRocks = true,
                                                    countCollapsedRocks = true))
        val rockCount = counter.calculateRockCount(_allRocks)
        assertEquals(_ascendedRocks.size, rockCount.ascended)
        assertEquals(_allRocks.size, rockCount.total)
    }

    @Test
    fun calculateRockCount_onlyOfficialAndPermittedRocksConfigured_countsOnlyAscendedRocks() {
        val counter = RockCounter(RockCounterConfig(countSummits = true,
                                                    countMassifs = true,
                                                    countBoulders = true,
                                                    countCaves = true))
        val rockCount = counter.calculateRockCount(_allRocks)
        assertEquals(_ascendedRocks.size, rockCount.ascended)
        assertEquals(_permittedRocks.size, rockCount.total)
    }

    @Test
    fun calculateRockCount_onlySummitsConfigured_countsOnlyAscendedSummits() {
        val counter = RockCounter(RockCounterConfig(countSummits = true))
        val rockCount = counter.calculateRockCount(_allRocks)
        assertEquals(_ascendedSummits.size, rockCount.ascended)
        assertEquals(_ascendedSummits.size, rockCount.total)
    }

    @Test
    fun calculateRockCount_onlySummitsAndOnlyLeadConfigured_countsOnlyLedSummits() {
        val counter = RockCounter(RockCounterConfig(countSummits = true,
                                                    countOnlyLeads = true))
        val rockCount = counter.calculateRockCount(_allRocks)
        assertEquals(_ledSummits.size, rockCount.ascended)
        assertEquals(_ascendedSummits.size, rockCount.total)
    }

}