package com.example.paetz.yacguide.map

import android.content.Context
import com.example.paetz.yacguide.R
import com.example.paetz.yacguide.map.cluster.BitmapManager
import com.example.paetz.yacguide.map.cluster.GeoItem
import com.example.paetz.yacguide.map.cluster.MarkerBitmap
import org.mapsforge.core.graphics.*
import org.mapsforge.core.model.Point
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import java.util.ArrayList

class RockBitmapManager(
        val context: Context
) : BitmapManager {
    private fun getMarkerBitmap(): List<MarkerBitmap> {
        val markerBitmaps: MutableList<MarkerBitmap> = ArrayList()

        val balloonGreen = context.getDrawable(R.drawable.map_marker_green)
        val markerGreen = AndroidGraphicFactory.convertToBitmap(balloonGreen)

        val balloonRed = context.getDrawable(R.drawable.map_marker_red)
        val markerRedS = AndroidGraphicFactory.convertToBitmap(balloonRed)

        val paint1 = AndroidGraphicFactory.INSTANCE.createPaint()
        paint1.setStyle(Style.STROKE)
        paint1.setTextAlign(Align.CENTER)
        paint1.setTypeface(FontFamily.DEFAULT, FontStyle.BOLD)
        paint1.setColor(Color.RED)

        markerBitmaps.add(MarkerBitmap(markerGreen, markerRedS,
                Point(0.0, 0.0), 10f, 1, paint1))

        // small icon. for 10 or less items.
        val balloonSN = context.getDrawable(R.drawable.balloon_n_s)
        val bitmapBalloonSN = AndroidGraphicFactory.convertToBitmap(balloonSN)
        bitmapBalloonSN.incrementRefCount()

        val balloonSS = context.getDrawable(R.drawable.balloon_n_n)
        val bitmapBalloonSS = AndroidGraphicFactory.convertToBitmap(balloonSS)

        val paint2 = AndroidGraphicFactory.INSTANCE.createPaint()
        paint2.setStyle(Style.FILL)
        paint2.setTextAlign(Align.CENTER)
        paint2.setTypeface(FontFamily.DEFAULT, FontStyle.BOLD)
        paint2.setColor(Color.WHITE)

        markerBitmaps.add(MarkerBitmap(bitmapBalloonSN,
                bitmapBalloonSS, Point(0.0, 0.0), 10f, 10, paint2))

        // large icon 100 will be ignored.
        val balloonMN = context.getDrawable(R.drawable.balloon_m_n)
        val bitmapBalloonMN = AndroidGraphicFactory.convertToBitmap(balloonMN)

        val balloonMS = context.getDrawable(R.drawable.balloon_m_s)
        val bitmapBalloonMS = AndroidGraphicFactory.convertToBitmap(balloonMS)

        val paint3 = AndroidGraphicFactory.INSTANCE.createPaint()
        paint3.setStyle(Style.FILL)
        paint3.setTextAlign(Align.CENTER)
        paint3.setTypeface(FontFamily.DEFAULT, FontStyle.BOLD)
        paint3.setColor(Color.WHITE)

        markerBitmaps.add(MarkerBitmap(bitmapBalloonMN,
                bitmapBalloonMS, Point(0.0, 0.0), 11f, Int.MAX_VALUE, paint3))

        return markerBitmaps
    }

    private val markerBmps = getMarkerBitmap()

    override fun getBitmap(items: List<GeoItem>): MarkerBitmap {
        for (bitmap in markerBmps) {
            if (items.size <= bitmap.itemMax) {
                return bitmap
            }
        }
        return markerBmps.last()
    }
}