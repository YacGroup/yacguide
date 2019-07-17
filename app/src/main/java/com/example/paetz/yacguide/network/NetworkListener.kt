package com.example.paetz.yacguide.network

interface NetworkListener {

    fun onNetworkTaskResolved(requestId: Int, result: String)

}
