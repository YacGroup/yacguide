package com.example.paetz.yacguide.network;

public class NetworkRequest {

    public static final int DATA_REQUEST_ID = 1;
    public static final int SUBDATA_REQUEST_ID = 2;
    public static final int COMMENTS_REQUEST_ID = 3;

    public int requestId;
    public String infoMessage;
    public String url;

    public NetworkRequest(int requestId, String infoMessage, String url) {
        this.requestId = requestId;
        this.infoMessage = infoMessage;
        this.url = url;
    }
}
