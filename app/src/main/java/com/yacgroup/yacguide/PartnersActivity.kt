/*
 * Copyright (C) 2019, 2022 Axel Paetzold
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

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseIntArray
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Partner
import com.yacgroup.yacguide.list_adapters.BaseViewItem
import com.yacgroup.yacguide.list_adapters.BaseViewAdapter
import com.yacgroup.yacguide.list_adapters.SwipeConfig
import com.yacgroup.yacguide.list_adapters.SwipeController
import com.yacgroup.yacguide.utils.DialogWidgetBuilder
import com.yacgroup.yacguide.utils.IntentConstants

import java.util.ArrayList

class PartnersActivity : AppCompatActivity() {

    private lateinit var _viewAdapter: BaseViewAdapter
    private lateinit var _db: DatabaseWrapper
    private lateinit var _selectedPartnerIds: MutableList<Int>
    private var _partnerNamePart: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partners)

        _db = DatabaseWrapper(this)
        _selectedPartnerIds = intent.getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS)
                .orEmpty().toMutableList()

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchEditText.onFocusChangeListener = View.OnFocusChangeListener { view, _ ->
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                _partnerNamePart = searchEditText.text.toString()
                _displayContent()
            }
        })

        _viewAdapter = BaseViewAdapter { partnerId -> _onPartnerSelected(partnerId) }
        val listView = findViewById<RecyclerView>(R.id.tableRecyclerView)
        listView.adapter = _viewAdapter

        val swipeRightConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorEdit),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_edit_24)!!
        ) { pos ->
            _updatePartnerListAndDB(pos) { partner ->
                _updatePartner(partner, R.string.dialog_text_change_partner)
            }
        }
        val swipeLeftConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorDelete),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_delete_24)!!
        ) { pos ->
            _updatePartnerListAndDB(pos) { partner ->
                _deletePartner(partner)
            }
        }
        val swipeController = SwipeController(swipeRightConfig, swipeLeftConfig)
        ItemTouchHelper(swipeController).attachToRecyclerView(listView)

        _displayContent()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            _saveAndLeave()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("UNUSED_PARAMETER")
    fun addPartner(v: View) {
        _updatePartner(null, R.string.dialog_text_add_partner)
    }

    private fun _saveAndLeave() {
        Intent().let {
            it.putIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS,
                    _selectedPartnerIds as ArrayList<Int>)
            setResult(Activity.RESULT_OK, it)
        }
        finish()
    }

    private fun _displayContent() {
        setTitle(R.string.title_climbing_partner)

        // We need to sort the partners according to the number of ascends you have done with them
        val ascendPartnerCount = SparseIntArray()
        _db.getAscends().forEach { ascend ->
            ascend.partnerIds?.forEach { id ->
                val prevValue = ascendPartnerCount.get(id, 0)
                ascendPartnerCount.put(id, prevValue + 1)
            }
        }

        val sortedPartners = _db.getPartners().sortedByDescending {
            ascendPartnerCount.get(it.id, 0)
        }.filter {
            it.name.orEmpty().lowercase().contains(_partnerNamePart.lowercase())
        }

        val partnerItemList = sortedPartners.map {
            if (_selectedPartnerIds.contains(it.id)) {
                BaseViewItem(
                    id = it.id,
                    name = "${getString(R.string.tick)} ${it.name.orEmpty()}",
                    additionalInfo = "(${ascendPartnerCount.get(it.id, 0)})",
                    backgroundResource = R.color.colorAccentLight)
            } else {
                BaseViewItem(
                    id = it.id,
                    name = "${getString(R.string.empty_box)} ${it.name.orEmpty()}",
                    additionalInfo = "(${ascendPartnerCount.get(it.id, 0)})",
                    backgroundResource = R.color.colorSecondaryLight)
            }
        }
        _viewAdapter.submitList(partnerItemList)
    }

    private fun _deletePartner(partner: Partner) {
        DialogWidgetBuilder(this, R.string.dialog_text_delete_partner).apply {
            setIcon(android.R.drawable.ic_dialog_alert)
            setNegativeButton()
            setPositiveButton { _, _ ->
                _db.deletePartner(partner)
                _selectedPartnerIds.remove(partner.id)
                _displayContent()
            }
        }.show()
    }

    private fun _updatePartner(partner: Partner?, dialogTitleResource: Int) {
        val view = layoutInflater.inflate(R.layout.add_partner_dialog, null).let { view ->
            partner?.let {
                view.findViewById<EditText>(R.id.addPartnerEditText).setText(it.name)
            }
            view
        }
        val dialog = DialogWidgetBuilder(this, dialogTitleResource).apply {
            setView(view)
            setNegativeButton()
            // Set to null. We override the onclick below.
            setPositiveButton(null)
        }.create()
        // This solution prevents that the dialog is closed automatically,
        // if the OK button is pressed even if no name is given or the name already exists.
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener{
                val newName = dialog.findViewById<EditText>(R.id.addPartnerEditText)
                        ?.text.toString().trim { it <= ' ' }
                when {
                    newName == "" ->
                        Toast.makeText(dialog.context, R.string.hint_no_name,
                                Toast.LENGTH_SHORT).show()
                    _db.getPartnerId(newName) > 0 ->
                        Toast.makeText(dialog.context, R.string.hint_name_already_used,
                                Toast.LENGTH_SHORT).show()
                    else -> {
                        val updatedPartner = partner ?: Partner()
                        updatedPartner.name = newName
                        _db.addPartner(updatedPartner)
                        dialog.dismiss()
                        _displayContent()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun _onPartnerSelected(partnerId: Int) {
        if (_selectedPartnerIds.contains(partnerId)) {
            _selectedPartnerIds.remove(partnerId)
        } else {
            _selectedPartnerIds.add(partnerId)
        }
        _displayContent()
    }

    private inline fun _updatePartnerListAndDB(position: Int, dbAction: (partner: Partner) -> Unit) {
        val item = _viewAdapter.getItemAt(position)
        _db.getPartner(item.id)?.let { dbAction(it) }
        _viewAdapter.notifyItemChanged(position)
    }
}
