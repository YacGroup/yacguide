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

package com.yacgroup.yacguide

import androidx.appcompat.app.AlertDialog
import android.content.Context

/*
 * Use this class for each dialog in the app because it sets some reasonable defaults
 * and avoids repeating code.
 */
class DialogWidgetBuilder(private var _context: Context): AlertDialog.Builder(_context) {

    init {
        setCancelable(false)
    }

    fun setNegativeButton() {
        // Closing the dialog is the default behaviour. Hence we don't implement a listener here.
        setNegativeButton(_context.getString(R.string.cancel), null)
    }
}