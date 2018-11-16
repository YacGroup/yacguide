package com.example.paetz.yacguide.network;

import org.jsoup.nodes.Document;

public interface NetworkListener {

    void onNetworkTaskResolved(Document result);

}
