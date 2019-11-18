package com.example.paetz.yacguide.map.cluster

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast

import org.mapsforge.core.model.BoundingBox
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.model.DisplayModel
import org.mapsforge.map.model.common.Observer
import org.mapsforge.map.view.MapView

import java.util.ArrayList
import java.util.Collections



class ClusterManager<T : GeoItem>(
        val context: Context,
        val mapView: MapView?,
        val bitmapManager: BitmapManager,
        private val maxClusteringZoom: Byte,
        val ignoreOnTap: Boolean) : Observer, SelectionHandler<T> {
    /**
     * grid size for Clustering(dip).
     */
    private val gridSize = 28 * DisplayModel.getDeviceScaleFactor()
    /**
     * Lock for the re-Clustering of the items.
     */
    var isClustering: Boolean = false
    /**
     * The current BoundingBox of the viewport.
     */
    private var currBoundingBox: BoundingBox? = null
    /**
     * GeoItem ArrayList object that are out of viewport and need to be
     * clustered on panning.
     */
    var leftItems: MutableList<T> = Collections.synchronizedList(ArrayList())
    /**
     * Clustered object list.
     */
    var clusters: MutableList<Cluster<T>> = Collections.synchronizedList(ArrayList())
    /**
     * Selected cluster
     */
    override var selectedItem: T? = null
    /**
     * saves the actual ZoolLevel of the MapView
     */
    private var oldZoomLevel: Double = -1.0
    /**
     * saves the actual Center as LatLong of the MapViewPosition
     */
    private var oldCenterLatLong: LatLong = LatLong(-90.0, -180.0)
    private var clusterTask: AsyncTask<Boolean, Void, Unit>? = null

    val allItems: List<T>
        @Synchronized get() {
            val rtnList = Collections.synchronizedList(ArrayList<T>())
            synchronized(leftItems) {
                rtnList.addAll(leftItems)
            }
            synchronized(clusters) {
                for (mCluster in clusters) {
                    rtnList.addAll(mCluster.items)
                }
            }
            return rtnList
        }

    val curBounds: BoundingBox?
        @Synchronized get() {
            if (currBoundingBox == null) {
                mapView?.let {
                    require((it.width > 0 && it.height > 0)) {
                        "width <= 0 or height <= 0"
                    }
                    val nw = mapView.mapViewProjection.fromPixels(0.0, 0.0)
                    val se = mapView.mapViewProjection.fromPixels(mapView.width.toDouble(),
                            mapView.height.toDouble())
                    if (nw != null && se != null) {
                        currBoundingBox = if (se.latitude > nw.latitude) {
                            BoundingBox(nw.latitude, se.longitude, se.latitude,
                                    nw.longitude)
                        } else {
                            BoundingBox(se.latitude, nw.longitude, nw.latitude,
                                    se.longitude)
                        }
                    }
                }

            }
            return currBoundingBox
        }

    override fun setSelectedItem(sender: SelectionHandler<T>?, selectedItem: T) {
        //        Log.d(TAG,"setSelectedItem(Selecting... (super) is called" );
        this.selectedItem = selectedItem
    }

    /**
     * add item and do isClustering. NOTE: this method will not redraw screen.
     * after adding all items, you must call redraw() method.
     *
     * @param item GeoItem to be clustered.
     */
    @Synchronized
    fun addItem(item: T) {
        // if mapView is not inflated or if not in viewport, add to leftItems
        if (mapView != null) {
            if (mapView.width == 0 || !isItemInViewport(item)) {

                synchronized(leftItems) {
                    if (clusterTask?.isCancelled == true) return
                    leftItems.add(item)
                }
            } else if (maxClusteringZoom >= mapView.model.mapViewPosition
                            .zoomLevel) {
                val pos = mapView.mapViewProjection.toPixels(item.latLong)

                synchronized(clusters) {
                    for (mCluster in clusters) {
                        if (clusterTask?.isCancelled == true) return
                        if (mCluster.items.isEmpty())
                            continue
                        // find a cluster which contains the marker.
                        // use 1st element to fix the location, hinder the cluster from
                        // running around and isClustering.
                        val gpCenter = mCluster.items[0].latLong
                        val ptCenter = mapView.mapViewProjection.toPixels(gpCenter)
                        // find a cluster which contains the marker.
                        if (pos.distance(ptCenter) <= gridSize) {
                            mCluster.addItem(item)
                            return
                        }
                    }
                    clusters.add(createCluster(item))
                }
            } else {
                synchronized(clusters) {
                    clusters.add(createCluster(item))
                }
            }
        }
    }

    /**
     * Create Cluster Object. override this method, if you want to use custom
     * GeoCluster class.
     *
     * @param item GeoItem to be set to cluster.
     */
    private fun createCluster(item: T): Cluster<T> {
        return Cluster(context, this, item)
    }

    /**
     * redraws clusters
     */
    @Synchronized
    fun redraw() {
        synchronized(clusters) {
            if (!isClustering)
                for (mCluster in clusters) {
                    mCluster.redraw()
                }
        }
    }

    /**
     * check if the item is within current viewport.
     *
     * @return true if item is within viewport.
     */
    private fun isItemInViewport(item: GeoItem): Boolean {
        val curBounds = curBounds
        return curBounds != null && curBounds.contains(item.latLong)
    }

    /**
     * add items that were not clustered in last isClustering.
     */
    fun addLeftItems() {
        if (leftItems.size == 0) {
            return
        }
        val currentLeftItems = ArrayList<T>()
        currentLeftItems.addAll(leftItems)
        synchronized(leftItems) {
            leftItems.clear()
        }
        for (currentLeftItem in currentLeftItems) {
            addItem(currentLeftItem)
        }
    }

    @Synchronized
    override fun onChange() {
        currBoundingBox = null
        if (isClustering) {
            return
        }

        if (mapView != null) {
            if (oldZoomLevel != mapView.model.mapViewPosition.zoomLevel.toDouble()) {
                // react on zoom changes
                // Log.d(TAG, "zooming...");
                oldZoomLevel = mapView.model.mapViewPosition.zoomLevel.toDouble()
                resetViewport(false)
            } else {
                // react on position changes
                val mapViewPosition = mapView.model.mapViewPosition

                val posOld = mapView.mapViewProjection.toPixels(oldCenterLatLong)
                val posNew = mapView.mapViewProjection.toPixels(mapViewPosition.center)
                if (posOld != null && posOld.distance(posNew) > gridSize / 2) {
                    oldCenterLatLong = mapViewPosition.center
                    resetViewport(true)
                }
            }
        }
    }

    /**
     * reset current viewport, re-cluster the items when zoom has changed, else
     * add not clustered items .
     */
    @Synchronized
    private fun resetViewport(isMoving: Boolean) {
        isClustering = true
        clusterTask = ClusterTask(this)
        clusterTask?.execute(isMoving)
    }

    private fun cancelClusterTask() {
        clusterTask?.cancel(true)
    }

    @Synchronized
    fun destroyGeoClusterer() {
        synchronized(clusters) {
            for (cluster in clusters) {
                cluster.clusterManager.cancelClusterTask()
                cluster.clear()
            }
            clusters.clear()
        }
        synchronized(leftItems) {
            leftItems.clear()
        }
        MarkerBitmap.clearCaptionBitmap()
    }

    companion object {

        var toast: Toast? = null
    }
}