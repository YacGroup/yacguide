package com.example.paetz.yacguide.map.cluster

import android.content.Context

import org.mapsforge.core.model.BoundingBox
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.Point
import org.mapsforge.core.model.Rectangle
import org.mapsforge.core.util.MercatorProjection
import org.mapsforge.map.layer.Layer
import java.lang.ref.WeakReference

/**
 * Layer extended class to display Clustered Marker.
 *
 * @param <T>
</T> */
class ClusterMarker<T : GeoItem>(
        private val context: Context,
        private val cluster: Cluster<T>,
        private val ignoreOnTap: Boolean) : Layer() {
    /**
     * icon marker type
     */
    private var markerType = 0
    /**
     * get center location of the marker.
     *
     * @return GeoPoint object of current marker center.
     */
    private val latLong: LatLong
        get() = cluster.location

    private val isSelected: Boolean
        get() = cluster.getItems().size == 1 && cluster.getItems()[0] === cluster.clusterManager.selectedItem

    /**
     * change icon bitmaps according to the state and content size.
     */
    private fun setMarkerBitmap() {
        markerType = 0
        while (markerType < cluster.clusterManager.markerIconBmps.size) {
            // Check if the number of items in this cluster is below or equal the limit of the MarkerBitMap
            if (cluster.getItems().size <= cluster.clusterManager.markerIconBmps[markerType].itemMax) {
                return
            }
            markerType++
        }
        // set the markerType to maximum value ==> reduce markerType by one.
        markerType--
    }

    @Synchronized
    override fun draw(boundingBox: BoundingBox, zoomLevel: Byte, canvas: org.mapsforge.core.graphics.Canvas, topLeftPoint: Point) {
        if (cluster.clusterManager.isClustering) {
            return
        }
        setMarkerBitmap()
        val mapSize = MercatorProjection.getMapSize(zoomLevel, this.displayModel.tileSize)
        val pixelX = MercatorProjection.longitudeToPixelX(this.latLong.longitude, mapSize)
        val pixelY = MercatorProjection.latitudeToPixelY(this.latLong.latitude, mapSize)

        val halfBitmapWidth: Double
        val halfBitmapHeight: Double
        try {
            halfBitmapWidth = (cluster.clusterManager.markerIconBmps[markerType].getBitmap(isSelected).width / 2f).toDouble()
            halfBitmapHeight = (cluster.clusterManager.markerIconBmps[markerType].getBitmap(isSelected).height / 2f).toDouble()
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return
        }

        val left = (pixelX - topLeftPoint.x - halfBitmapWidth + cluster.clusterManager.markerIconBmps[markerType].iconOffset.x).toInt()
        val top = (pixelY - topLeftPoint.y - halfBitmapHeight + cluster.clusterManager.markerIconBmps[markerType].iconOffset.y).toInt()
        val right = left + cluster.clusterManager.markerIconBmps[markerType].getBitmap(isSelected)
                .width
        val bottom = top + cluster.clusterManager.markerIconBmps[markerType].getBitmap(isSelected)
                .height
        val mBitmapRectangle = Rectangle(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble())
        val canvasRectangle = Rectangle(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
        if (!canvasRectangle.intersects(mBitmapRectangle)) {
            return
        }
        // Draw bitmap
        canvas.drawBitmap(cluster.clusterManager.markerIconBmps[markerType].getBitmap(isSelected), left, top)

        // Draw Text
        if (markerType == 0) {
            // Draw bitmap
            val bubble = MarkerBitmap.getBitmapFromTitle(context, cluster.title,
                    cluster.clusterManager.markerIconBmps[markerType].paint)
            canvas.drawBitmap(bubble,
                    (left + halfBitmapWidth - bubble.width / 2).toInt(),
                    top - bubble.height)
        } else {
            val x = (left + halfBitmapWidth).toInt()
            val y = (top.toDouble() + halfBitmapHeight
                    + (cluster.clusterManager.markerIconBmps[markerType].paint.getTextHeight(cluster.title) / 2).toDouble()).toInt()
            canvas.drawText(cluster.title, x, y,
                    cluster.clusterManager.markerIconBmps[markerType].paint)
        }
    }

    /**
     * @return Gets the LatLong Position of the Layer Object
     */
    override fun getPosition(): LatLong? {
        return latLong
    }

    @Synchronized
    override fun onTap(geoPoint: LatLong?, viewPosition: Point?,
                       tapPoint: Point?): Boolean {
        if (ignoreOnTap) return false
        if (viewPosition == null || tapPoint == null) return false

        if (cluster.getItems().size == 1 && contains(viewPosition, tapPoint)) {
            cluster.clusterManager.setSelectedItem(null, cluster.getItems()[0])
            requestRedraw()
            return true
        } else if (contains(viewPosition, tapPoint)) {
            val mText = StringBuilder(cluster.getItems().size.toString() + " items:")
            for (i in 0 until cluster.getItems().size) {
                mText.append("\n- ")
                mText.append(cluster.getItems()[i].title)
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

    @Synchronized
    fun contains(viewPosition: Point, tapPoint: Point): Boolean {
        return getBitmapRectangle(viewPosition).contains(tapPoint)
    }

    private fun getBitmapRectangle(center: Point): Rectangle {
        val isSelected = isSelected
        return Rectangle(
                center.x - cluster.clusterManager.markerIconBmps[markerType]
                        .getBitmap(isSelected).width.toFloat() + cluster.clusterManager.markerIconBmps[markerType].iconOffset.x,
                center.y - cluster.clusterManager.markerIconBmps[markerType]
                        .getBitmap(isSelected).height.toFloat() + cluster.clusterManager.markerIconBmps[markerType].iconOffset.y,
                center.x
                        + cluster.clusterManager.markerIconBmps[markerType]
                        .getBitmap(isSelected).width.toFloat().toDouble()
                        + cluster.clusterManager.markerIconBmps[markerType].iconOffset.x,
                center.y
                        + cluster.clusterManager.markerIconBmps[markerType]
                        .getBitmap(isSelected).height.toFloat().toDouble()
                        + cluster.clusterManager.markerIconBmps[markerType].iconOffset.y)
    }
}
