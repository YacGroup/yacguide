package com.example.paetz.yacguide.utils

import org.json.JSONException
import org.json.JSONObject

import java.util.HashMap
import java.util.regex.Pattern

object ParserUtils {

    // There are some czech letters with ambiguous utf-8 encodings.
    // Map structure: < wrong_utf8_as_regex, correct_utf8 >
    private val specialCzechCharacterUtf8Map = object : HashMap<String, String>() {
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
        for (wrongCodeRegex in specialCzechCharacterUtf8Map) {
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
