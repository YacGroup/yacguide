package com.example.paetz.yacguide.map.cluster

interface BitmapManager {
    fun getBitmap(items: List<GeoItem>): MarkerBitmap
}
