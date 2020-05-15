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

package com.yacgroup.yacguide

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View

import com.yacgroup.yacguide.database.DatabaseWrapper

abstract class TableActivity : BaseNavigationActivity() {
    protected lateinit var db: DatabaseWrapper
    protected lateinit var customSettings: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseWrapper(this)
        customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)
    }

    open fun showComments(v: View) {}

    override fun onResume() {
        super.onResume()

        displayContent()
    }

    protected fun prepareCommentDialog(): Dialog {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.comment_dialog)
        dialog.findViewById<View>(R.id.closeButton).setOnClickListener { dialog.dismiss() }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()

        return dialog
    }

    protected abstract fun displayContent()
}
