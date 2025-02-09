/*
 * Copyright (C) 2021, 2023 Christian Sommer
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

import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.drawable.toBitmap
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.extensions.getPackageInfoCompat

open class ActivityUtils(protected val activity: AppCompatActivity) {

    val appVersion = _getAppVersion()

    private fun _getAppVersion(): String {
        try {
            val pkgManager = activity.packageManager
            val pkgInfo = pkgManager.getPackageInfoCompat(activity.packageName, 0)
            return pkgInfo.versionName ?: "0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "0.0"
    }

    fun openUrl(url: String) {
        if (URLUtil.isValidUrl(url)) {
            CustomTabsIntent.Builder().apply {
                AppCompatResources.getDrawable(activity, R.drawable.ic_baseline_arrow_back_24)
                    ?.let { setCloseButtonIcon(it.toBitmap()) }
            }.build().launchUrl(activity, Uri.parse(url))
        }
    }
}
