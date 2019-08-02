/*
 * Copyright 2009 Huan Erdao
 * Copyright 2014 Martin Vennekamp
 * Copyright 2015 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.paetz.yacguide.map.cluster

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable

import android.view.Gravity
import android.view.View
import android.widget.TextView

import com.example.paetz.yacguide.R

import org.mapsforge.core.graphics.Bitmap
import org.mapsforge.core.graphics.Paint
import org.mapsforge.core.model.Point
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.model.DisplayModel

import java.util.HashMap
import kotlin.math.absoluteValue

/**
 * Utility Class to handle MarkerBitmap
 * it handles grid offset to display on the map with offset
 */
class MarkerBitmap
(
 /**
  * bitmap object for normal state icon
  */
 private val iconBmpNormal: Bitmap,
 /**
  * bitmap object for select state icon
  */
 private val iconBmpSelect: Bitmap,
 /**
  * offset grid of icon in Point.
  * if you are using symmetric icon image, it should be half size of width&height.
  * adjust this parameter to offset the axis of the image.
  */
 val iconOffset: Point,
 private var textSize: Float,
 val itemMax: Int,
 val paint: Paint) {
    init {
        this.textSize = textSize * DisplayModel.getDeviceScaleFactor()
        this.paint.setTextSize(textSize)
        iconBmpNormal.incrementRefCount()
        iconBmpSelect.incrementRefCount()
    }

    /**
     * @return bitmap object for normal state icon or for for select state icon
     */
    fun getBitmap(isSelected: Boolean): Bitmap {
        return if (isSelected) {
            iconBmpSelect
        } else
            iconBmpNormal
    }

    companion object {

        private val captionViews = HashMap<String, Bitmap>()

        fun getBitmapFromTitle(context: Context, title: String, paint: Paint): Bitmap {
            if (!captionViews.containsKey(title)) {
                val bubbleView = TextView(context)
                bubbleView.background = context.getDrawable(R.drawable.caption_background)
                bubbleView.gravity = Gravity.CENTER
                bubbleView.maxEms = 40
                bubbleView.textSize = 9f
                bubbleView.setPadding(5, -4, 5, -4)
                bubbleView.setTextColor(android.graphics.Color.BLACK)
                bubbleView.text = title
                //Measure the view at the exact dimensions (otherwise the text won't center correctly)
                val height = paint.getTextHeight(title) + bubbleView.paddingTop.absoluteValue + bubbleView.paddingBottom.absoluteValue
                val width = paint.getTextWidth(title) + bubbleView.paddingLeft + bubbleView.paddingRight
                val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                bubbleView.measure(widthSpec, heightSpec)

                //Layout the view at the width and height
                bubbleView.layout(0, 0, bubbleView.measuredWidth, bubbleView.measuredHeight)

                captionViews[title] = viewToBitmap(context, bubbleView)
                captionViews[title]?.incrementRefCount()
            }
            return captionViews[title]!!
        }

        private fun viewToBitmap(c: Context, view: View): Bitmap {
            val renderTarget = android.graphics.Bitmap.createBitmap(view.measuredWidth,
                    view.measuredHeight, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = Canvas(renderTarget)
            view.draw(canvas)
            return AndroidGraphicFactory.convertToBitmap(BitmapDrawable(c.resources, renderTarget))
        }

        fun clearCaptionBitmap() {
            for (bitmap in captionViews.values){
                bitmap.decrementRefCount()
            }
            captionViews.clear()
        }
    }
}
