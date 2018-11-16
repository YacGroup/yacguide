package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;

public abstract class HTMLParser {

    protected static String baseUrl = "http://db-sandsteinklettern.gipfelbuch.de";

    protected UpdateListener listener;
    protected AppDatabase db;

    public HTMLParser(AppDatabase db, UpdateListener listener) {
        this.listener = listener;
        this.db = db;
    }

    public abstract void parse();
}
