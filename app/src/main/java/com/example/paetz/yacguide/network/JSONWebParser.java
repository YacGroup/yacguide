package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;

import org.json.JSONException;

import java.util.LinkedList;

public abstract class JSONWebParser implements NetworkListener {

    protected static final String baseUrl = "http://db-sandsteinklettern.gipfelbuch.de/";

    protected AppDatabase db;
    protected UpdateListener listener;
    protected LinkedList<NetworkRequest> networkRequests;

    public JSONWebParser(AppDatabase db, UpdateListener listener) {
        this.db = db;
        this.listener = listener;
        networkRequests = new LinkedList<NetworkRequest>(); // filled in subclasses
    }

    @Override
    public void onNetworkTaskResolved(int requestId, String result) {
        boolean success = true;
        try {
            if (result == null) {
                throw new JSONException("");
            }
            parseData(requestId, result);
        } catch (JSONException e) {
            success = false;
        } finally {
            listener.onEvent(success, "");
        }
    }

    public void fetchData() {
        for (NetworkRequest request : networkRequests) {
            listener.onEvent(true, request.infoMessage);
            (new NetworkTask(request.requestId, this)).execute(request.url);
        }
    }

    protected void parseComments(String json) {

    }

    protected abstract void parseData(int requestId, String json) throws JSONException;
}
