/*
 * Copyright (C) 2023 Christian Sommer
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

package com.yacgroup.yacguide.database.tourbook

import android.content.ContentResolver
import android.net.Uri
import com.yacgroup.yacguide.database.DatabaseWrapper
import java.io.FileOutputStream
import java.io.IOException

abstract class BaseExporter {
    lateinit var contentResolver: ContentResolver
    lateinit var db: DatabaseWrapper

    abstract fun export(uri: Uri)

    @Throws(IOException::class)
    fun writeStrToUri(uri: Uri, str: String) {
        contentResolver.openFileDescriptor(uri, "w")?.use {
            FileOutputStream(it.fileDescriptor).write(
                str.toByteArray(Charsets.UTF_8)
            )
        }
    }
}
