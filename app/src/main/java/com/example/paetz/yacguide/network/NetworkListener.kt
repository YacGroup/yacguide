package com.example.paetz.yacguide.network

interface NetworkListener {

    fun onNetworkTaskResolved(requestId: NetworkRequestType, result: String)

}
