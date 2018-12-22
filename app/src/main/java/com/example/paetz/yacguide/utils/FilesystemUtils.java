package com.example.paetz.yacguide.utils;

import android.os.Environment;

public class FilesystemUtils {

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state)
             || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

}
