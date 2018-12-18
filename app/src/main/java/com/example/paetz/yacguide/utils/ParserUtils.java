package com.example.paetz.yacguide.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtils {

    // There are some czech letters with ambiguous utf-8 encodings.
    // Map structure: < wrong_utf8_as_regex, correct_utf8 >
    private static final Map<String, String> _specialCzechCharacterUtf8Map = new HashMap<String, String>() {{
        put("\\\\u008a", "\u0160"); // big š
        put("\\\\u008e", "\u017d"); // big ž
        put("\\\\u009a", "\u0161"); // small š
        put("\\\\u009e", "\u017e"); // small ž
    }};

    public static String resolveToUtf8(String text) {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = Pattern.compile("&#([0-9]+);").matcher(text);
        while (matcher.find()) {
            int charUnicode;
            try {
                charUnicode = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                charUnicode = 783; // "?"
            }
            matcher.appendReplacement(buffer, (new Character((char) charUnicode)).toString());
        }
        matcher.appendTail(buffer);

        String result = buffer.toString();
        for (final String wrongCodeRegex : _specialCzechCharacterUtf8Map.keySet()) {
            result = result.replaceAll(wrongCodeRegex, _specialCzechCharacterUtf8Map.get(wrongCodeRegex));
        }
        return result;
    }

    public static int jsonField2Int(JSONObject jsonObject, String fieldName) throws JSONException {
        try {
            return Integer.parseInt(jsonObject.getString(fieldName));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static float jsonField2Float(JSONObject jsonObject, String fieldName) throws JSONException {
        try {
            return Float.parseFloat(jsonObject.getString(fieldName));
        } catch (NumberFormatException e) {
            return 0.f;
        }
    }

    public static char jsonField2Char(JSONObject jsonObject, String fieldName) throws JSONException {
        try {
            return jsonObject.getString(fieldName).charAt(0);
        } catch (IndexOutOfBoundsException e) {
            return ' ';
        }
    }

    public static String jsonField2String(JSONObject jsonObject, String fieldName, String defaultFieldName) throws JSONException {
        final String value = jsonObject.getString(fieldName);
        return (value.isEmpty() ? jsonObject.getString(defaultFieldName) : value);
    }
}
