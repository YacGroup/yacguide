package com.yacgroup.yacguide.network

interface NetworkListener {

    fun onNetworkTaskResolved(requestId: NetworkRequestType, result: String)

}
