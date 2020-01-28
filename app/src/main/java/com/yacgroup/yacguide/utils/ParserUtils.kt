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

import org.json.JSONException
import org.json.JSONObject

import java.util.HashMap
import java.util.regex.Pattern

object ParserUtils {

    // There are some czech letters with ambiguous utf-8 encodings.
    // Map structure: < wrong_utf8_as_regex, correct_utf8 >
    private val _specialCzechCharacterUtf8Map = object : HashMap<String, String>() {
        init {
            put("\\\\u008a", "\u0160") // big š
            put("\\\\u008e", "\u017d") // big ž
            put("\\\\u009a", "\u0161") // small š
            put("\\\\u009e", "\u017e") // small ž
        }
    }

    fun resolveToUtf8(text: String): String {
        val buffer = StringBuffer()
        val matcher = Pattern.compile("&#([0-9]+);").matcher(text)
        while (matcher.find()) {
            val charUnicode: Int = try {
                Integer.parseInt(matcher.group(1))
            } catch (e: NumberFormatException) {
                783 // "?"
            }

            matcher.appendReplacement(buffer, charUnicode.toChar().toString())
        }
        matcher.appendTail(buffer)

        var result = buffer.toString()
        for (wrongCodeRegex in _specialCzechCharacterUtf8Map) {
            result = result.replace(wrongCodeRegex.key.toRegex(), wrongCodeRegex.value)
        }
        return result
    }

    @Throws(JSONException::class)
    fun jsonField2Int(jsonObject: JSONObject, fieldName: String): Int {
        return try {
            Integer.parseInt(jsonObject.getString(fieldName))
        } catch (e: NumberFormatException) {
            0
        }

    }

    @Throws(JSONException::class)
    fun jsonField2Float(jsonObject: JSONObject, fieldName: String): Float {
        return try {
            java.lang.Float.parseFloat(jsonObject.getString(fieldName))
        } catch (e: NumberFormatException) {
            0f
        }

    }

    @Throws(JSONException::class)
    fun jsonField2Char(jsonObject: JSONObject, fieldName: String): Char {
        return try {
            jsonObject.getString(fieldName)[0]
        } catch (e: IndexOutOfBoundsException) {
            ' '
        }

    }

    @Throws(JSONException::class)
    fun jsonField2String(jsonObject: JSONObject, fieldName: String, defaultFieldName: String): String {
        val value = jsonObject.getString(fieldName)
        return if (value.isEmpty()) jsonObject.getString(defaultFieldName) else value
    }
}
