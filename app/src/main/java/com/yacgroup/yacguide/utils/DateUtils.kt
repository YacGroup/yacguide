package com.yacgroup.yacguide.utils

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar

object DateUtils {

    const val UNKNOWN_DATE = "0000-00-00"

    @SuppressLint("SimpleDateFormat")
    fun formatDate(date: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd")
        return try {
            val formattedDate = format.parse(date)
            val cal = Calendar.getInstance()
            cal.time = formattedDate
            "${cal.get(Calendar.DAY_OF_MONTH)}.${cal.get(Calendar.MONTH) + 1}.${cal.get(Calendar.YEAR)}"
        } catch (e: ParseException) {
            ""
        }

    }
}
