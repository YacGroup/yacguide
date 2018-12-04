package com.example.paetz.yacguide.network;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkTask extends AsyncTask<String, Void, String> {

    private int _requestId;
    private NetworkListener _listener;

    public NetworkTask(int requestId, NetworkListener listener) {
        _requestId = requestId;
        _listener = listener;
    }

    @Override
    protected String doInBackground(String... urls) {
        if (urls.length != 1) {
            System.err.println("Can only process one url");
            return null;
        }
        HttpURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpURLConnection) new URL(urls[0]).openConnection();
            InputStream inStream = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream resStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) != -1) {
                resStream.write(buffer, 0, length);
            }
            result = resStream.toString("UTF-8");
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        _listener.onNetworkTaskResolved(_requestId, result);
    }
}
