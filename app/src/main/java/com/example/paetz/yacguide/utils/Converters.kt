package com.example.paetz.yacguide.utils

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
