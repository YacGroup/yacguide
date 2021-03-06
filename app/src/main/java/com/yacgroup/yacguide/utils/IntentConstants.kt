/*
 * Copyright (C) 2019 Fabian Kantereit
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

package com.yacgroup.yacguide.utils

object IntentConstants {
    const val COUNTRY_KEY = "CountryName"
    const val REGION_KEY = "RegionId"
    const val SECTOR_KEY = "SectorId"
    const val ROCK_KEY = "RockId"
    const val ROUTE_KEY = "RouteId"
    const val ASCEND_KEY = "AscendId"
    const val ASCEND_PARTNER_IDS = "AscendPartnerIds"
    const val SELECTED_ROCK_IDS = "SelectedRockIds"

    // Result codes
    const val RESULT_UPDATED = 1000

    // Request codes
    const val REQUEST_OPEN_TOURBOOK = 2002
    const val REQUEST_SAVE_TOURBOOK = 2003
}
