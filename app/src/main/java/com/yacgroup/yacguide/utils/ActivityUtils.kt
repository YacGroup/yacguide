/*
 * Copyright (C) 2021 Christian Sommer
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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yacgroup.yacguide.R

open class ActivityUtils(protected val activity: AppCompatActivity) {

    val appVersion: String = _getAppVersion()

    private fun _getAppVersion(): String {
        try {
            val pkgManager = activity.packageManager
            val pkgInfo = pkgManager.getPackageInfo(activity.packageName, 0)
            return pkgInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "0.0"
    }

    fun openUrl(url: String) {
        if (URLUtil.isValidUrl(url)) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                activity.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    activity,
                    R.string.no_webbrowser_available, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}