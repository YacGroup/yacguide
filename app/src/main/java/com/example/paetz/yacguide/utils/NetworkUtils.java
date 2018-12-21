package com.example.paetz.yacguide.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {

    // I couldn't find a proper java library, so let's escape it on our own
    private static final Map<String, String> _URL_ENCODE_MAP = new HashMap<String, String>() {{
        put(" ", "%20");
        put("Ä", "%C4");
        put("ä", "%E4");
        put("Ö", "%D6");
        put("ö", "%F6");
        put("Ü", "%DC");
        put("ü", "%FC");
        put("ß", "%DF");
    }};

    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String encodeString2Url(String str) {
        for (Map.Entry<String, String> specialChar : _URL_ENCODE_MAP.entrySet()) {
            str = str.replace(specialChar.getKey(), specialChar.getValue());
        }
        return str;
    }
}
