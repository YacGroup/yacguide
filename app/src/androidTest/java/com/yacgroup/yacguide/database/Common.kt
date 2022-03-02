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

import com.yacgroup.yacguide.equal
import org.junit.jupiter.api.Assertions.assertTrue

/*********************************************************************
 * Database structure:
 * - Country1:
 *      - Region1
 *          - Sector1
 *          - Sector2
 *      - Region2
 *          - Sector3
 * - Country2
 *      - Region3
 * - Country3
 ********************************************************************/
class TestDB {

    companion object {
        const val INVALID_NAME = "???"
        const val INVALID_ID = 42

        val COUNTRIES = listOf(
            Country("Country1"),
            Country("Country2"),
            Country("Country3")
        )

        val REGIONS = listOf(
            Region(1, "Region1", "Country1"),
            Region(2, "Region2", "Country1"),
            Region(3, "Region3", "Country2")
        )

        val SECTORS = listOf(
            Sector(1, 1f, "Sector1", 1),
            Sector(2, 2f, "Sector2", 1),
            Sector(3, 3f, "Sector3", 2)
        )

        fun initCountries(countryDao: CountryDao) {
            assertTrue(countryDao.all.isEmpty())
            countryDao.insert(COUNTRIES)
            assertTrue(equal(COUNTRIES, countryDao.all))
        }

        fun initRegions(regionDao: RegionDao) {
            assertTrue(regionDao.all.isEmpty())
            regionDao.insert(REGIONS)
            assertTrue(equal(REGIONS, regionDao.all))
        }

        fun initSectors(sectorDao: SectorDao) {
            assertTrue(sectorDao.all.isEmpty())
            sectorDao.insert(SECTORS)
            assertTrue(equal(SECTORS, sectorDao.all))
        }
    }
}