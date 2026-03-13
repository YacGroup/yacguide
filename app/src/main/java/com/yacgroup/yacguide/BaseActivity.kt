/*
 * Copyright (C) 2026 Christian Sommer
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

package com.yacgroup.yacguide

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<ViewBindingType: ViewBinding> : AppCompatActivity() {

    protected lateinit var activityViewBinding: ViewBindingType
    protected var toolbar: Toolbar? = null

    abstract fun getViewBinding(): ViewBindingType

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        activityViewBinding = getViewBinding()
        setContentView(activityViewBinding.root)

        // Set status bar icons to white.
        //
        // By default, on many devices, the system assumes a light background and shows dark icons.
        // When you target API 35/36 and "colorize" the status bar area with a dark color
        // (like your colorPrimary), the black icons become hard to see.
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        toolbar = activityViewBinding.root.findViewById<Toolbar>(R.id.toolbar).also {
            if (it != null) {
                setSupportActionBar(it)
                // Enable the Up button by default. Can be disabled, if required.
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }

        // Support edge-to-edge window mode which was introduced in API 15 and enforced for API 16+.
        ViewCompat.setOnApplyWindowInsetsListener(activityViewBinding.root) { rootView, windowInsets ->
            windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).let { insets ->
                rootView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = insets.left
                    bottomMargin = insets.bottom
                    rightMargin = insets.right
                }
                rootView.findViewById<android.view.View>(R.id.appbar).let { appBar ->
                    if (appBar != null) {
                        // Stretch root view to top edge
                        rootView.updatePadding(top = 0)
                        // Move app bar down by insets
                        appBar.updatePadding(top = insets.top)
                    } else {
                        // If no custom appbar is found, apply top margin to root to avoid overlap.
                        rootView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            topMargin = insets.top
                        }
                    }
                }
                windowInsets
            }
        }
    }

}
