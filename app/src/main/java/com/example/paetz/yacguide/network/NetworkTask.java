package com.example.paetz.yacguide.network;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class NetworkTask extends AsyncTask<String, Void, Document> {

    private NetworkListener _listener;

    public NetworkTask(NetworkListener listener) {
        _listener = listener;
    }

    @Override
    protected Document doInBackground(String... urls) {
        if (urls.length != 1) {
            System.err.println("Can only process one url");
            return null;
        }
        try {
            return Jsoup.connect(urls[0]).get();
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Document result) {
        if (result == null) {
            System.err.println("ERROR: Document is null");
            return;
        }
        _listener.onNetworkTaskResolved(result);
    }
}
