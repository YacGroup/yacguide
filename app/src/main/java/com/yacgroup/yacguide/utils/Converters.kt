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

import android.arch.persistence.room.TypeConverter
import android.text.TextUtils
import java.util.ArrayList

class Converters {
    @TypeConverter
    fun fromString(intListAsString: String): ArrayList<Int> {
        val intList = ArrayList<Int>()
        if (intListAsString.isNotEmpty()) {
            val strList = intListAsString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (str in strList) {
                intList.add(Integer.parseInt(str.trim { it <= ' ' }))
            }
        }
        return intList
    }

    @TypeConverter
    fun fromIntList(intList: ArrayList<Int>): String {
        return TextUtils.join(",", intList)
    }

}
