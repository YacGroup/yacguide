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
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

object WidgetUtils {

    const val tableFontSizeDp = 18
    const val infoFontSizeDp = 16
    const val textFontSizeDp = 14
    // That must be in sync with the color definition colorSecondaryLight.
    // FIXME: Find a better solution to get this color string.
    val tourHeaderColor: Int
        get() = Color.parseColor("#cfcfcf")

    class Padding (val left: Int, val top: Int, val right: Int, val bottom: Int)

    fun createCommonRowLayout(context: Context,
                              textLeft: String,
                              textRight: String = "",
                              textSizeDp: Int = tableFontSizeDp,
                              onClickListener: View.OnClickListener = View.OnClickListener {},
                              bgColor: Int = Color.WHITE,
                              typeface: Int = Typeface.BOLD,
                              padding: Padding = Padding(20, 20, 20, 10)): RelativeLayout {
        val layout = RelativeLayout(context)
        layout.setBackgroundColor(bgColor)
        layout.setOnClickListener(onClickListener)

        val textViewLeft = createTextView(context, textLeft, textSizeDp, typeface, padding)
        layout.addView(textViewLeft)
        textViewLeft.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)

        val textViewRight = createTextView(context, textRight, textSizeDp, typeface, padding)
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
                               typeface: Int,
                               padding: Padding): TextView {
        val textView = TextView(context)
        textView.id = View.generateViewId()
        textView.text = text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeDp.toFloat())
        textView.setTypeface(textView.typeface, typeface)
        textView.setPadding(padding.left, padding.top, padding.right, padding.bottom)

        return textView
    }
}
