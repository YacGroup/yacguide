package com.example.paetz.yacguide.map

import com.example.paetz.yacguide.map.cluster.GeoItem
import org.mapsforge.core.model.LatLong

class RockGeoItem(
        val id: Int,
        override val title: String,
        override val latLong: LatLong
) : GeoItem
