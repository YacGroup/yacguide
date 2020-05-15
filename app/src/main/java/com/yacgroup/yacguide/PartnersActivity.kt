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
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseIntArray
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.yacgroup.yacguide.database.DatabaseWrapper

import com.yacgroup.yacguide.database.Partner
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.WidgetUtils

import java.util.ArrayList
import java.util.HashMap

class PartnersActivity : AppCompatActivity() {

    private lateinit var _db: DatabaseWrapper
    private var _checkboxMap: MutableMap<Int, CheckBox> = HashMap()
    private lateinit var _selectedPartnerIds: List<Int>
    private var _partnerNamePart: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partners)

        _db = DatabaseWrapper(this)
        _selectedPartnerIds = intent.getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS).orEmpty()

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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            _saveAndLeave()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun addPartner(v: View) {
        val dialog = Dialog(this)
        dialog.setTitle(R.string.dialog_text_add_partner)
        dialog.setContentView(R.layout.add_partner_dialog)
        val okButton = dialog.findViewById<Button>(R.id.okButton)
        okButton.setOnClickListener {
            val newName = dialog.findViewById<EditText>(R.id.addPartnerEditText).text.toString().trim { it <= ' ' }
            _updatePartner(dialog, newName, null)
        }
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun _saveAndLeave() {
        val selectedIds = ArrayList<Int>()
        for ((key, cb) in _checkboxMap) {
            if (cb.isChecked) {
                selectedIds.add(key)
            }
        }
        val resultIntent = Intent()
        resultIntent.putIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS, selectedIds)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun _displayContent() {
        this.setTitle(R.string.title_climbing_partner)
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        _checkboxMap.clear()

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
            val partnerName = partner.name.orEmpty()
            val innerLayout = RelativeLayout(this)
            innerLayout.setBackgroundColor(Color.WHITE)

            val checkBox = CheckBox(this)
            checkBox.text = partnerName
            if (_selectedPartnerIds.contains(partner.id)) {
                checkBox.isChecked = true
            }
            _checkboxMap[partner.id] = checkBox
            innerLayout.addView(checkBox)

            val editButton = ImageButton(this)
            editButton.id = View.generateViewId()
            editButton.setImageResource(android.R.drawable.ic_menu_edit)
            editButton.setOnClickListener {
                val dialog = Dialog(this@PartnersActivity)
                dialog.setContentView(R.layout.add_partner_dialog)
                dialog.findViewById<EditText>(R.id.addPartnerEditText).setText(partnerName)
                val okButton = dialog.findViewById<Button>(R.id.okButton)
                okButton.setOnClickListener {
                    val newName = dialog.findViewById<EditText>(R.id.addPartnerEditText).text.toString().trim { it <= ' ' }
                    _updatePartner(dialog, newName, partner)
                }
                val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
                cancelButton.setOnClickListener { dialog.dismiss() }
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(false)
                dialog.show()
            }
            innerLayout.addView(editButton)

            val deleteButton = ImageButton(this)
            deleteButton.id = View.generateViewId()
            deleteButton.setImageResource(android.R.drawable.ic_menu_delete)
            deleteButton.setOnClickListener {
                val dialog = Dialog(this@PartnersActivity)
                dialog.setContentView(R.layout.dialog)
                dialog.findViewById<TextView>(R.id.dialogText).setText(R.string.dialog_text_delete_partner)
                val okButton = dialog.findViewById<Button>(R.id.yesButton)
                okButton.setOnClickListener {
                    _db.deletePartner(partner)
                    dialog.dismiss()
                    _displayContent()
                }
                val cancelButton = dialog.findViewById<Button>(R.id.noButton)
                cancelButton.setOnClickListener { dialog.dismiss() }
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(false)
                dialog.show()
            }
            innerLayout.addView(deleteButton)

            val buttonWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics).toInt()
            val paramsDelete = deleteButton.layoutParams as RelativeLayout.LayoutParams
            paramsDelete.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            paramsDelete.height = buttonWidthPx
            paramsDelete.width = paramsDelete.height

            val paramsEdit = editButton.layoutParams as RelativeLayout.LayoutParams
            paramsEdit.addRule(RelativeLayout.LEFT_OF, deleteButton.id)
            paramsEdit.height = buttonWidthPx
            paramsEdit.width = paramsEdit.height

            layout.addView(innerLayout)
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    private fun _updatePartner(dialog: Dialog, newName: String, partner: Partner?) {
        when {
            newName == "" ->
                Toast.makeText(dialog.context, R.string.hint_no_name, Toast.LENGTH_SHORT).show()
            _db.getPartnerId(newName) > 0 ->
                Toast.makeText(dialog.context, R.string.hint_name_already_used, Toast.LENGTH_SHORT).show()
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
