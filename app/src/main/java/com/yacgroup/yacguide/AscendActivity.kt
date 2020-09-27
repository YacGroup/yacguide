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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner

import com.yacgroup.yacguide.database.Ascend
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.IntentConstants

import java.util.ArrayList
import java.util.Calendar

class AscendActivity : AppCompatActivity() {

    private lateinit var _db: DatabaseWrapper
    private var _ascend: Ascend? = null
    private var _route: Route? = null
    private var _partnerIds: ArrayList<Int>? = null
    private var _styleId: Int = 0
    private var _year: Int = 0
    private var _month: Int = 0
    private var _day: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ascend)

        _db = DatabaseWrapper(this)

        _ascend = _db.getAscend(intent.getIntExtra(IntentConstants.ASCEND_KEY, DatabaseWrapper.INVALID_ID))
        val routeId = intent.getIntExtra(IntentConstants.ROUTE_KEY, DatabaseWrapper.INVALID_ID)

        _route = _db.getRoute(routeId.takeUnless { it == DatabaseWrapper.INVALID_ID }
                ?: _ascend?.routeId ?: DatabaseWrapper.INVALID_ID)
        // Beware: _route may still be null (if the route of this ascend has been deleted meanwhile)
        _partnerIds = intent.getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS)
                ?: _ascend?.partnerIds ?: ArrayList()

        findViewById<View>(R.id.notesEditText).onFocusChangeListener = View.OnFocusChangeListener { view, _ ->
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        _displayContent()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            _partnerIds = data?.getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS) ?: ArrayList()
            _displayContent()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun enter(v: View) {
        val existingAscend = _ascend

        val ascend = Ascend()
        ascend.routeId = _route?.id ?: existingAscend?.routeId ?: DatabaseWrapper.INVALID_ID
        ascend.styleId = _styleId
        ascend.year = _year
        ascend.month = _month
        ascend.day = _day
        ascend.partnerIds = _partnerIds
        ascend.notes = findViewById<EditText>(R.id.notesEditText).text.toString()

        if (existingAscend != null) {
            ascend.id = existingAscend.id
            _db.deleteAscend(existingAscend)
        }

        _db.addAscend(ascend)
        val resultIntent = Intent()
        setResult(IntentConstants.RESULT_UPDATED, resultIntent)
        finish()
    }

    @Suppress("UNUSED_PARAMETER")
    fun cancel(v: View) {
        val resultIntent = Intent()
        setResult(Activity.RESULT_CANCELED, resultIntent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    @Suppress("UNUSED_PARAMETER")
    fun enterDate(v: View) {
        val calendar = Calendar.getInstance()
        val yy = calendar.get(Calendar.YEAR)
        val mm = calendar.get(Calendar.MONTH)
        val dd = calendar.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(this@AscendActivity, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            _year = year
            _month = monthOfYear + 1
            _day = dayOfMonth
            (findViewById<View>(R.id.dateEditText) as EditText).setText("$_day.$_month.$_year")
        }, yy, mm, dd)
        datePicker.show()
    }

    @Suppress("UNUSED_PARAMETER")
    fun selectPartners(v: View) {
        val partnerNames = findViewById<EditText>(R.id.partnersEditText).text.toString().split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val partnerIds = _db.getPartnerIds(partnerNames.toList()) as ArrayList<Int>
        val intent = Intent(this@AscendActivity, PartnersActivity::class.java)
        intent.putIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS, partnerIds)
        startActivityForResult(intent, 0)
    }

    @SuppressLint("SetTextI18n")
    private fun _displayContent() {
        title = _route?.let { "${it.name}   ${it.grade}" } ?: DatabaseWrapper.UNKNOWN_NAME

        val spinner = findViewById<Spinner>(R.id.styleSpinner)
        val adapter = ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, AscendStyle.names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                _styleId = AscendStyle.fromName(parent.getItemAtPosition(position).toString())?.id ?: 0
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                _styleId = 0
            }
        }

        val existingAscend = _ascend
        if (existingAscend != null) {
            _day = existingAscend.day
            _month = existingAscend.month
            _year = existingAscend.year
            findViewById<EditText>(R.id.dateEditText).setText("$_day.$_month.$_year")
            findViewById<EditText>(R.id.notesEditText).setText(existingAscend.notes)
            spinner.setSelection(adapter.getPosition(AscendStyle.fromId(if (_styleId == 0) existingAscend.styleId else _styleId)?.styleName))
        } else {
            spinner.setSelection(if (_styleId != 0) adapter.getPosition(AscendStyle.fromId(_styleId)?.styleName) else 0)
        }

        val partnerNames = _db.getPartnerNames(_partnerIds.orEmpty().toList())
        findViewById<EditText>(R.id.partnersEditText).setText(TextUtils.join(", ", partnerNames))
    }
}
