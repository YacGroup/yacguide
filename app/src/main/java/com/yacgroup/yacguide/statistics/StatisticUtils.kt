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

package com.yacgroup.yacguide.statistics

import android.util.SparseIntArray
import com.yacgroup.yacguide.database.Ascend

class StatisticUtils {

    companion object {

        fun getAscendCountsPerPartner(ascends: List<Ascend>): SparseIntArray {
            val ascendCounts = SparseIntArray()
            ascends.forEach { ascend ->
                ascend.partnerIds?.forEach { id ->
                    val prevValue = ascendCounts.get(id, 0)
                    ascendCounts.put(id, prevValue + 1)
                }
            }
            return ascendCounts;
        }

    }
}