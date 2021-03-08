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

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat

import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.utils.AscendStyle

abstract class TableActivity : BaseNavigationActivity() {
    protected lateinit var db: DatabaseWrapper
    protected lateinit var customSettings: SharedPreferences

    protected enum class ElementFilter {
        eNone,
        eOfficial,
        eProject
    }
    protected val filterMap = mapOf(
        0 to ElementFilter.eNone,
        1 to ElementFilter.eOfficial,
        2 to ElementFilter.eProject
    )

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

    protected fun prepareCommentDialog(): AlertDialog {
        val dialog = AlertDialog.Builder(this).apply {
            setTitle(R.string.comments)
            setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            setView(R.layout.comment_dialog)
        }.create()
        // we need to call show() before findViewById can be used.
        dialog.show()
        return dialog
    }

    protected fun showNoCommentToast() {
        Toast.makeText(this, R.string.no_comment_available, Toast.LENGTH_SHORT).show()
    }

    protected fun colorizeEntry(ascendsBitMask: Int, defaultColor: Int): Int {
        return when {
            AscendStyle.isLead(ascendsBitMask) -> customSettings.getInt(getString(R.string.lead), ContextCompat.getColor(this, R.color.color_lead))
            AscendStyle.isFollow(ascendsBitMask) -> customSettings.getInt(getString(R.string.follow), ContextCompat.getColor(this, R.color.color_follow))
            else -> defaultColor
        }
    }

    protected fun decorateEntry(ascendsBitMask: Int): String {
        val botchAdd = if (AscendStyle.isBotch(ascendsBitMask)) getString(R.string.botch) else ""
        val projectAdd = if (AscendStyle.isProject(ascendsBitMask)) getString(R.string.project) else ""
        val watchingAdd = if (AscendStyle.isWatching(ascendsBitMask)) getString(R.string.watching) else ""
        return "$botchAdd$projectAdd$watchingAdd"
    }

    protected abstract fun displayContent()
}
