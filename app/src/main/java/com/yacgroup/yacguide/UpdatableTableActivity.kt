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
import android.content.Intent
import android.graphics.Typeface
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.yacgroup.yacguide.database.Rock

import com.yacgroup.yacguide.network.JSONWebParser
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.NetworkUtils
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.WidgetUtils

abstract class UpdatableTableActivity : TableActivity(), UpdateListener {

    private var _updateDialog: Dialog? = null
    protected var jsonParser: JSONWebParser? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sync_delete, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> _searchRockGlobally()
            R.id.action_download -> update()
            R.id.action_delete -> _delete()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    // UpdateListener
    override fun onUpdateFinished(success: Boolean) {
        _updateDialog?.dismiss()

        if (success) {
            Toast.makeText(this, R.string.objects_refreshed, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.error_on_refresh, Toast.LENGTH_SHORT).show()
        }
        displayContent()
    }

    override fun onUpdateStatus(statusMessage: String) {
        runOnUiThread {
            _updateDialog?.findViewById<TextView>(R.id.dialogText)?.text = statusMessage
        }
    }

    fun update(v: View = View(this)) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show()
            return
        }
        jsonParser?.fetchData()
        showUpdateDialog()
    }

    private fun _delete() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog)
        (dialog.findViewById<TextView>(R.id.dialogText)).setText(R.string.dialog_question_delete)
        dialog.findViewById<Button>(R.id.yesButton).setOnClickListener {
            deleteContent()
            dialog.dismiss()
            Toast.makeText(applicationContext, R.string.objects_deleted, Toast.LENGTH_SHORT).show()
            displayContent()
        }
        dialog.findViewById<Button>(R.id.noButton).setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun showUpdateDialog() {
        _updateDialog = Dialog(this)
        _updateDialog?.setContentView(R.layout.info_dialog)
        _updateDialog?.setCancelable(false)
        _updateDialog?.setCanceledOnTouchOutside(false)
        _updateDialog?.show()
    }

    protected fun displayDownloadButton() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        val downloadButton = layoutInflater.inflate(R.layout.button_download, null) as ImageButton
        layout.addView(downloadButton)
    }

    protected abstract fun deleteContent()

    protected abstract fun searchRocks(rockName: String): List<Rock>

    private fun _searchRockGlobally() {
        val searchDialog = Dialog(this)
        searchDialog.setContentView(R.layout.search_dialog)
        searchDialog.findViewById<Button>(R.id.searchButton).setOnClickListener {
            val rockName = searchDialog.findViewById<EditText>(R.id.dialogEditText).text.toString()
            if (rockName.isEmpty()) {
                Toast.makeText(searchDialog.context, R.string.hint_no_name, Toast.LENGTH_SHORT).show()
            } else {
                val rocks = searchRocks(rockName)
                if (rocks.isEmpty()) {
                    Toast.makeText(searchDialog.context, R.string.hint_name_not_found, Toast.LENGTH_SHORT).show()
                } else {
                    _displaySelectedRocks(rocks)
                    searchDialog.dismiss()
                }
            }
        }
        searchDialog.findViewById<Button>(R.id.cancelButton).setOnClickListener { searchDialog.dismiss() }
        searchDialog.setCancelable(false)
        searchDialog.setCanceledOnTouchOutside(false)
        searchDialog.show()
    }

    private fun _displaySelectedRocks(rocks: List<Rock>) {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        for (rock in rocks) {
            val rockName = ParserUtils.decodeObjectNames(rock.name)
            val onClickListener = View.OnClickListener {
                val intent = Intent(this, RouteActivity::class.java)
                intent.putExtra(IntentConstants.ROCK_KEY, rock.id)
                startActivity(intent)
            }
            val sector = db.getSector(rock.parentId)!!
            val sectorName = ParserUtils.decodeObjectNames(sector.name)
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = db.getRegion(sector.parentId)?.name.orEmpty(),
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    onClickListener = onClickListener,
                    typeface = Typeface.NORMAL))
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = sectorName.first,
                    textRight = sectorName.second,
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    onClickListener = onClickListener))
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = rockName.first,
                    textRight = rockName.second,
                    onClickListener = onClickListener))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }
}
