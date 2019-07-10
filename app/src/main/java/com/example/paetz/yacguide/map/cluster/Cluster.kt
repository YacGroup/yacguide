package com.example.paetz.yacguide.map.cluster

import android.content.Context
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.layer.Layers
import java.lang.ref.WeakReference

import java.util.ArrayList
import java.util.Collections

/**
 * Cluster class.
 * contains single marker object(ClusterMarker). mostly wraps methods in ClusterMarker.
 */
class Cluster<T : GeoItem>
(
        context: Context,
        val clusterManager: ClusterManager<T>,
        item: T) {
    /**
     * Center of cluster
     */
    var location: LatLong = item.latLong
        private set
    /**
     * List of GeoItem within cluster
     */
    private val items = Collections.synchronizedList(ArrayList<T>())
    /**
     * ClusterMarker object
     */
    private val clusterMarker: ClusterMarker<T> = ClusterMarker(context, this, clusterManager.ignoreOnTap)

    val title: String
        get() = if (getItems().size == 1) {
            getItems()[0].title
        } else getItems().size.toString()

    init {
        addItem(item)
    }

    /**
     * add item to cluster object
     *
     * @param item GeoItem object to be added.
     */
    @Synchronized
    fun addItem(item: T) {
        synchronized(items) {
            items.add(item)
        }

        // computing the centroid
        var lat = 0.0
        var lon = 0.0
        var n = 0
        synchronized(items) {
            for (`object` in items) {
                lat += `object`.latLong.latitude
                lon += `object`.latLong.longitude
                n++
            }
        }
        location = LatLong(lat / n, lon / n)
    }


    /**
     * get list of GeoItem.
     *
     * @return list of GeoItem within cluster.
     */
    @Synchronized
    fun getItems(): List<T> {
        synchronized(items) {
            return items
        }
    }

    /**
     * clears cluster object and removes the cluster from the layers collection.
     */
    fun clear() {
        val mapOverlays = clusterManager.mapView!!.layerManager.layers
        if (mapOverlays.contains(clusterMarker)) {
            mapOverlays.remove(clusterMarker)
        }
        synchronized(items) {
            items.clear()
        }
    }

    /**
     * add the ClusterMarker to the Layers if is within Viewport, otherwise remove.
     */
    fun redraw() {
        val mapOverlays = clusterManager.mapView!!.layerManager.layers
        if (!clusterManager.curBounds!!.contains(location) && mapOverlays.contains(clusterMarker)) {
            mapOverlays.remove(clusterMarker)
            return
        }
        if (mapOverlays.size() > 0 && !mapOverlays.contains(clusterMarker)
                && !clusterManager.isClustering) {
            mapOverlays.add(1, clusterMarker)
        }
    }
}
