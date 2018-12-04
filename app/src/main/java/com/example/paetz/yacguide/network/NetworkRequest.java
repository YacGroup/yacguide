package com.example.paetz.yacguide.network;

public class NetworkRequest {

    public static final int DATA_REQUEST_ID = 1;
    public static final int SUBDATA_REQUEST_ID = 2;
    public static final int COMMENTS_REQUEST_ID = 3;

    public int requestId;
    public String url;

    public NetworkRequest(int requestId, String url) {
        this.requestId = requestId;
        this.url = url;
    }
}
