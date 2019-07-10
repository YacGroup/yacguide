package com.example.paetz.yacguide.map.cluster

import org.mapsforge.core.model.LatLong

interface GeoItem {
    val latLong: LatLong
    val title: String
}
