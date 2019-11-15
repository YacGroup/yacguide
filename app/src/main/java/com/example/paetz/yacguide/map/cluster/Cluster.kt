package com.example.paetz.yacguide.map.cluster

import android.content.Context
import org.mapsforge.core.model.BoundingBox
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.Point
import org.mapsforge.core.model.Rectangle
import org.mapsforge.core.util.MercatorProjection
import org.mapsforge.map.layer.Layer

import java.util.ArrayList
import java.util.Collections

/**
 * Cluster class.
 * contains single marker object(ClusterMarker). mostly wraps methods in ClusterMarker.
 */
class Cluster<T : GeoItem>(
        val context: Context,
        val clusterManager: ClusterManager<T>,
        item: T
) : Layer() {
    /**
     * Center of cluster
     */
    var location: LatLong = item.latLong
        private set
    /**
     * List of GeoItem within cluster
     */
    val items: MutableList<T> = Collections.synchronizedList(ArrayList<T>())

    val title: String
        get() = if (items.size == 1) {
            items[0].title
        } else items.size.toString()

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
        var n: Int
        synchronized(items) {
            for (i in items) {
                lat += i.latLong.latitude
                lon += i.latLong.longitude
            }
            n = items.size
        }
        location = LatLong(lat / n, lon / n)
    }

    /**
     * clears cluster object and removes the cluster from the layers collection.
     */
    fun clear() {
        clusterManager.mapView?.layerManager?.layers?.let {
            if (it.contains(this)) {
                it.remove(this)
            }
        }
        synchronized(items) {
            items.clear()
        }
    }

    /**
     * add the ClusterMarker to the Layers if is within Viewport, otherwise remove.
     */
    fun redraw() {
        clusterManager.mapView?.layerManager?.layers?.let { mapOverlays ->
            if ((clusterManager.curBounds?.contains(location) != true) && mapOverlays.contains(this)) {
                mapOverlays.remove(this)
                return
            }
            if (mapOverlays.size() > 0 && !mapOverlays.contains(this)
                    && !clusterManager.isClustering) {
                mapOverlays.add(1, this)
            }
        }
    }

    private val isSelected: Boolean
        get() = items.size == 1 && items[0] === clusterManager.selectedItem

    @Synchronized
    override fun draw(boundingBox: BoundingBox, zoomLevel: Byte, canvas: org.mapsforge.core.graphics.Canvas, topLeftPoint: Point) {
        if (clusterManager.isClustering) {
            return
        }
        val bitmap = clusterManager.bitmapManager.getBitmap(items)
        val mapSize = MercatorProjection.getMapSize(zoomLevel, this.displayModel.tileSize)
        val pixelX = MercatorProjection.longitudeToPixelX(this.location.longitude, mapSize)
        val pixelY = MercatorProjection.latitudeToPixelY(this.location.latitude, mapSize)

        val halfBitmapWidth = bitmap.getBitmap(isSelected).width / 2f
        val halfBitmapHeight = bitmap.getBitmap(isSelected).height / 2f

        val left = (pixelX - topLeftPoint.x - halfBitmapWidth + bitmap.iconOffset.x)
        val top = (pixelY - topLeftPoint.y - halfBitmapHeight + bitmap.iconOffset.y)
        val right = left + bitmap.getBitmap(isSelected).width
        val bottom = top + bitmap.getBitmap(isSelected).height

        val mBitmapRectangle = Rectangle(left, top, right, bottom)
        val canvasRectangle = Rectangle(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
        if (!canvasRectangle.intersects(mBitmapRectangle)) {
            return
        }
        // Draw bitmap
        canvas.drawBitmap(bitmap.getBitmap(isSelected), left.toInt(), top.toInt())

        // Draw Text
        if (bitmap.itemMax == 1) {
            // Draw bitmap
            val bubble = MarkerBitmap.getBitmapFromTitle(context, title, bitmap.paint)
            canvas.drawBitmap(bubble,
                    (left + halfBitmapWidth - bubble.width / 2).toInt(),
                    top.toInt() - bubble.height)
        } else {
            val x = (left + halfBitmapWidth).toInt()
            val y = (top + halfBitmapHeight
                    + (bitmap.paint.getTextHeight(title) / 2).toDouble()).toInt()
            canvas.drawText(title, x, y, bitmap.paint)
        }
    }

    @Synchronized
    override fun onTap(geoPoint: LatLong?, viewPosition: Point?,
              tapPoint: Point?): Boolean {
        if (clusterManager.ignoreOnTap) return false
        if (viewPosition == null || tapPoint == null) return false

        if (items.size == 1 && contains(viewPosition, tapPoint)) {
            clusterManager.setSelectedItem(null, items[0])
            requestRedraw()
            return true
        } else if (contains(viewPosition, tapPoint)) {
            val mText = StringBuilder(items.size.toString() + " items:")
            for (i in items.indices) {
                mText.append("\n- ")
                mText.append(items[i].title)
                if (i == 7) {
                    mText.append("\n...")
                    break
                }
            }
            ClusterManager.toast?.setText(mText)
            ClusterManager.toast?.show()
        }
        return false
    }

    /**
     * @return Gets the LatLong Position of the Layer Object
     */
    override fun getPosition(): LatLong? {
        return location
    }


    @Synchronized
    fun contains(viewPosition: Point, tapPoint: Point): Boolean {
        return getBitmapRectangle(viewPosition).contains(tapPoint)
    }

    private fun getBitmapRectangle(center: Point): Rectangle {
        val isSelected = isSelected
        val bitmap = clusterManager.bitmapManager.getBitmap(items)
        return Rectangle(
                center.x - bitmap.getBitmap(isSelected).width + bitmap.iconOffset.x,
                center.y - bitmap.getBitmap(isSelected).height + bitmap.iconOffset.y,
                center.x + bitmap.getBitmap(isSelected).width + bitmap.iconOffset.x,
                center.y + bitmap.getBitmap(isSelected).height + bitmap.iconOffset.y)
    }

}
