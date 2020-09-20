/*
 * Copyright (C) 2019 Fabian Kantereit
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.yacgroup.yacguide.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

object WidgetUtils {

    const val tableFontSizeDp = 18
    const val infoFontSizeDp = 16
    const val textFontSizeDp = 14
    const val tourHeaderColor = -0x444445

    fun createCommonRowLayout(context: Context,
                              textLeft: String,
                              textRight: String,
                              textSizeDp: Int,
                              onClickListener: View.OnClickListener,
                              bgColor: Int,
                              typeFace: Int): RelativeLayout {
        return createCommonRowLayout(context, textLeft, textRight, textSizeDp, onClickListener, bgColor, typeFace, 20, 20, 20, 10)
    }

    fun createCommonRowLayout(context: Context,
                              textLeft: String,
                              textRight: String,
                              textSizeDp: Int,
                              onClickListener: View.OnClickListener,
                              bgColor: Int,
                              typeFace: Int,
                              vararg padding: Int): RelativeLayout {
        val layout = RelativeLayout(context)
        layout.setBackgroundColor(bgColor)
        layout.setOnClickListener(onClickListener)

        if (padding.size != 4) {
            return layout
        }

        val textViewLeft = createTextView(context, textLeft, textSizeDp, typeFace, padding[0], padding[1], padding[2], padding[3])
        layout.addView(textViewLeft)
        textViewLeft.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)

        val textViewRight = createTextView(context, textRight, textSizeDp, typeFace, padding[0], padding[1], padding[2], padding[3])
        layout.addView(textViewRight)
        textViewRight.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT)

        val paramsRight = textViewRight.layoutParams as RelativeLayout.LayoutParams
        paramsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
        val paramsLeft = textViewLeft.layoutParams as RelativeLayout.LayoutParams
        paramsLeft.addRule(RelativeLayout.LEFT_OF, textViewRight.id)

        return layout
    }

    fun createHorizontalLine(context: Context, size: Int): View {
        val lineView = View(context)
        lineView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, size)
        lineView.setBackgroundColor(Color.parseColor("#000000"))

        return lineView
    }

    private fun createTextView(context: Context,
                               text: String,
                               textSizeDp: Int,
                               typeFace: Int,
                               paddingLeft: Int,
                               paddingTop: Int,
                               paddingRight: Int,
                               paddingBottom: Int): TextView {
        val textView = TextView(context)
        textView.id = View.generateViewId()
        textView.text = text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeDp.toFloat())
        textView.setTypeface(textView.typeface, typeFace)
        textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

        return textView
    }
}
