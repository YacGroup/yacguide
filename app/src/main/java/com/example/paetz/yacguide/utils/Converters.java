package com.example.paetz.yacguide.utils;

import android.arch.persistence.room.TypeConverter;
import android.text.TextUtils;
import java.util.ArrayList;

public class Converters {
    @TypeConverter
    public static ArrayList<Integer> fromString(String intListAsString) {
        ArrayList<Integer> intList = new ArrayList<Integer>();
        if (!intListAsString.isEmpty()) {
            String[] strList = intListAsString.split(",");
            for (String str : strList) {
                intList.add(Integer.parseInt(str.trim()));
            }
        }
        return intList;
    }

    @TypeConverter
    public static String fromIntList(ArrayList<Integer> intList) {
        return TextUtils.join(",", intList);
    }

}
