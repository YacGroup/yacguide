package com.example.paetz.yacguide.utils;

import android.os.Environment;

public class FilesystemUtils {

    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
