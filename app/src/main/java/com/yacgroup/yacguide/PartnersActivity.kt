/*
 * Copyright (C) 2019, 2022, 2023 Axel Paetzold
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

package com.yacgroup.yacguide

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.SparseIntArray
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Partner
import com.yacgroup.yacguide.list_adapters.*
import com.yacgroup.yacguide.utils.*

import java.util.ArrayList

class PartnersActivity : AppCompatActivity() {

    private val _ascendPartnerCount = SparseIntArray()
    private lateinit var _customSettings: SharedPreferences
    private lateinit var _searchBarHandler: SearchBarHandler
    private lateinit var _viewAdapter: ListViewAdapter<Partner>
    private lateinit var _visualUtils: VisualUtils
    private lateinit var _db: DatabaseWrapper
    private lateinit var _selectedPartnerIds: MutableList<Int>
    private var _partnerNamePart: String = ""
    private var _sortAlphabetically: Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partners)

        _db = DatabaseWrapper(this)
        _customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)
        _selectedPartnerIds = intent.getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS)
                .orEmpty().toMutableList()

        _searchBarHandler = SearchBarHandler(
            searchBarLayout = findViewById(R.id.searchBarLayout),
            searchHintResource = R.string.partner_search,
            _checkBoxTitle = getString(R.string.sort_alphabetically),
            checkBoxDefaultValue = resources.getBoolean(R.bool.sort_alphabetically),
            _settings = _customSettings,
            initCallback = { sortAlphabetically -> _sortAlphabetically = sortAlphabetically },
            updateCallback = { partnerNamePart, sortAlphabetically -> _onSearchBarUpdate(partnerNamePart, sortAlphabetically) }
        )

        _viewAdapter = ListViewAdapter(ItemDiffCallback(
            _areItemsTheSame = { partner1, partner2 -> partner1.id == partner2.id },
            _areContentsTheSame = { partner1, partner2 -> partner1 == partner2 }
        )) { partner -> ListItem(
            backgroundColor = _getPartnerBackground(partner),
            mainText = _getPartnerMainText(partner),
            subText = "(${_ascendPartnerCount.get(partner.id)})",
            onClick = { _onPartnerSelected(partner) })
        }
        val listView = findViewById<RecyclerView>(R.id.tableRecyclerView)
        listView.adapter = _viewAdapter

        val swipeRightConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorEdit),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_edit_24)!!
        ) { viewHolder ->
            _updatePartnerListAndDB(viewHolder as ListViewAdapter<Partner>.ItemViewHolder) { partner ->
                _updatePartner(partner, R.string.dialog_text_change_partner)
            }
        }
        val swipeLeftConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorDelete),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_delete_24)!!
        ) { viewHolder ->
            _updatePartnerListAndDB(viewHolder as ListViewAdapter<Partner>.ItemViewHolder) { partner ->
                _deletePartner(partner)
            }
        }
        val swipeController = SwipeController(swipeRightConfig, swipeLeftConfig)
        ItemTouchHelper(swipeController).attachToRecyclerView(listView)

        _visualUtils = VisualUtils(this)

        _displayContent()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            _saveAndLeave()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        _searchBarHandler.storeCustomSettings()
        super.onStop()
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
        setTitle(R.string.partner)

        // We need to sort the partners according to the number of ascends you have done with them
        _ascendPartnerCount.clear()
        _db.getAscendsBelowStyleId(AscendStyle.eBOTCHED.id).forEach { ascend ->
            ascend.partnerIds?.forEach { id ->
                val prevValue = _ascendPartnerCount.get(id, 0)
                _ascendPartnerCount.put(id, prevValue + 1)
            }
        }

        val filteredPartners =
            if (_partnerNamePart.isNotEmpty())
                _db.getPartners().filter { it.name.orEmpty().lowercase().contains(_partnerNamePart.lowercase()) }
            else
                _db.getPartners()
        val sortedPartners =
            if (_sortAlphabetically)
                filteredPartners.sortedBy { it.name.orEmpty().lowercase() }
            else
                filteredPartners.sortedByDescending { _ascendPartnerCount.get(it.id, 0) }
        _viewAdapter.submitList(sortedPartners)
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
                        val updatedPartner = partner?.apply { name = newName } ?: Partner(name = newName)
                        _db.addPartner(updatedPartner)
                        dialog.dismiss()
                        _displayContent()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun _getPartnerBackground(partner: Partner): Int {
        return if (_selectedPartnerIds.contains(partner.id))
                _visualUtils.accentBgColor
            else
                _visualUtils.defaultBgColor
    }

    private fun _getPartnerMainText(partner: Partner): Pair<String, String> {
        return if (_selectedPartnerIds.contains(partner.id))
                Pair("${_visualUtils.tickedBoxIcon} ${partner.name.orEmpty()}", "")
            else
                Pair("${_visualUtils.emptyBoxIcon} ${partner.name.orEmpty()}", "")
    }

    private fun _onPartnerSelected(partner: Partner) {
        if (_selectedPartnerIds.contains(partner.id)) {
            _selectedPartnerIds.remove(partner.id)
        } else {
            _selectedPartnerIds.add(partner.id)
        }
        val partnerPosition = _viewAdapter.currentList.indexOfFirst { it.id == partner.id }
        _viewAdapter.notifyItemChanged(partnerPosition)
    }

    private inline fun _updatePartnerListAndDB(viewHolder: ListViewAdapter<Partner>.ItemViewHolder, dbAction: (partner: Partner) -> Unit) {
        viewHolder.getItem()?.let { partner ->
            _db.getPartner(partner.id)?.let { dbAction(it) }
        }
        _viewAdapter.notifyItemChanged(viewHolder.adapterPosition)
    }

    private fun _onSearchBarUpdate(partnerNamePart: String, sortAlphabetically: Boolean) {
        _partnerNamePart = partnerNamePart
        _sortAlphabetically = sortAlphabetically
        _displayContent()
    }
}
