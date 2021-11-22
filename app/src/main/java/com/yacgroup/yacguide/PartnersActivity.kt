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

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseIntArray
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast

import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Partner
import com.yacgroup.yacguide.utils.DialogWidgetBuilder
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

import java.util.ArrayList

class PartnersActivity : AppCompatActivity() {

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

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        _displayContent()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item?.itemId == android.R.id.home) {
            _saveAndLeave()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("UNUSED_PARAMETER")
    fun addPartner(v: View) {
        _updatePartner(null, getString(R.string.dialog_text_add_partner))
    }

    private fun _saveAndLeave() {
        Intent().let {
            it.putIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS,
                    _selectedPartnerIds as ArrayList<Int>)
            setResult(Activity.RESULT_OK, it)
        }
        finish()
    }

    // Method is called, if the partner check box changes.
    private fun _checkPartner(partnerId: Int, isChecked: Boolean) {
        if (isChecked) {
            _selectedPartnerIds.add(partnerId)
        } else {
            _selectedPartnerIds.remove(partnerId)
        }
    }

    private fun _displayContent() {
        this.setTitle(R.string.title_climbing_partner)
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()

        // We need to sort the partners according to the number of ascends you have done with them
        val ascendPartnerCount = SparseIntArray()
        for (ascend in _db.getAscends()) {
            ascend.partnerIds?.let {
                for (id in it) {
                    val prevValue = ascendPartnerCount.get(id, 0)
                    ascendPartnerCount.put(id, prevValue + 1)
                }
            }
        }

        val sortedPartners = _db.getPartners().sortedByDescending {
            ascendPartnerCount.get(it.id, 0)
        }.filter {
            it.name.orEmpty().toLowerCase().contains(_partnerNamePart.toLowerCase())
        }

        for (partner in sortedPartners) {
            val innerLayout = RelativeLayout(this)
            innerLayout.setBackgroundColor(Color.WHITE)

            CheckBox(this).let {
                it.text = partner.name.orEmpty()
                it.isChecked = _selectedPartnerIds.contains(partner.id)
                // Associate partner ID to button ID for later identification.
                it.id = partner.id
                it.setOnCheckedChangeListener { buttonView, isChecked ->
                    _checkPartner(buttonView.id, isChecked)
                }
                innerLayout.addView(it)
            }

            val editButton = ImageButton(this).apply {
                id = View.generateViewId()
                setImageResource(android.R.drawable.ic_menu_edit)
                setOnClickListener {
                    _updatePartner(partner, getString(R.string.dialog_text_change_partner))
                }
            }
            innerLayout.addView(editButton)

            val deleteButton = ImageButton(this).apply {
                id = View.generateViewId()
                setImageResource(android.R.drawable.ic_menu_delete)
                setOnClickListener {
                    _deletePartner(partner)
                }
            }
            innerLayout.addView(deleteButton)

            val buttonWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f,
                    resources.displayMetrics).toInt()
            (deleteButton.layoutParams as RelativeLayout.LayoutParams).let {
                it.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
                it.height = buttonWidthPx
                it.width = it.height
            }

            (editButton.layoutParams as RelativeLayout.LayoutParams).let {
                it.addRule(RelativeLayout.LEFT_OF, deleteButton.id)
                it.height = buttonWidthPx
                it.width = it.height
            }

            layout.addView(innerLayout)
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
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

    private fun _updatePartner(partner: Partner?, dialogTitle: String) {
        val view = layoutInflater.inflate(R.layout.add_partner_dialog, null).let { view ->
            partner?.let {
                view.findViewById<EditText>(R.id.addPartnerEditText).setText(it.name)
            }
            view
        }
        val dialog = DialogWidgetBuilder(this, dialogTitle).apply {
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
}
