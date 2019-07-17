package com.example.paetz.yacguide.network;

public class NetworkRequest {

    public NetworkRequestType requestId;
    public String url;

    public NetworkRequest(NetworkRequestType requestId, String url) {
        this.requestId = requestId;
        this.url = url;
    }
}
