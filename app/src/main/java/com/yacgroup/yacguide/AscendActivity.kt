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
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.text.TextWatcher
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
import com.yacgroup.yacguide.utils.ParserUtils

import java.util.ArrayList
import java.util.Calendar

class AscendActivity : AppCompatActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _ascend: Ascend
    private var _route: Route? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ascend)

        _db = DatabaseWrapper(this)

        // The database query is different from null,
        // if this activity is called from the TourbookActivity or TourbookAscendActivity.
        _ascend = _db.getAscend(intent.getIntExtra(IntentConstants.ASCEND_KEY,
                DatabaseWrapper.INVALID_ID)) ?: Ascend()
        // The intent constant ROUTE_KEY is only available, if this activity is called from
        // the route DescriptionActivity.
        val routeId = intent.getIntExtra(IntentConstants.ROUTE_KEY, DatabaseWrapper.INVALID_ID)
        _route = _db.getRoute(routeId.takeUnless { it == DatabaseWrapper.INVALID_ID }
                ?: _ascend.routeId)
        // Beware: _route may still be null (if the route of this ascend has been deleted meanwhile)

        findViewById<EditText>(R.id.notesEditText).let {
            it.onFocusChangeListener = View.OnFocusChangeListener { view, _ ->
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    _ascend.notes = s.toString()
                }
            })
        }

        _displayContent()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            _ascend.partnerIds = data?.getIntegerArrayListExtra(IntentConstants.ASCEND_PARTNER_IDS)
                    ?: ArrayList()
            _displayContent()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun enter(v: View) {
        _ascend.routeId = _route?.id ?: _ascend.routeId
        // For new ascends id == 0!
        if (_ascend.id != 0) {
            _db.deleteAscend(_ascend)
        }
        _db.addAscend(_ascend)
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
        val yy = _ascend.year.takeIf { it != 0 } ?: calendar.get(Calendar.YEAR)
        val mm = _ascend.month.takeIf { it != 0 }.let { it?.minus(1) } ?: calendar.get(Calendar.MONTH)
        val dd = _ascend.day.takeIf { it != 0 } ?: calendar.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(this@AscendActivity, { _, year, monthOfYear, dayOfMonth ->
            _ascend.year = year
            _ascend.month = monthOfYear + 1
            _ascend.day = dayOfMonth
            (findViewById<View>(R.id.dateEditText) as EditText).setText(
                    "${_ascend.day}.${_ascend.month}.${_ascend.year}")
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
        val routeName = ParserUtils.decodeObjectNames(_route?.name)
        this.title = "${if (routeName.first.isNotEmpty()) routeName.first else routeName.second} ${_route?.grade.orEmpty()}"

        val spinner = findViewById<Spinner>(R.id.styleSpinner)
        val adapter = ArrayAdapter<CharSequence>(this, R.layout.spinner_item, AscendStyle.names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                _ascend.styleId = AscendStyle.fromName(parent.getItemAtPosition(position).toString())?.id ?: 0
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                _ascend.styleId = 0
            }
        }
        spinner.setSelection(adapter.getPosition(AscendStyle.fromId(_ascend.styleId)?.styleName))

        if (_ascend.day != 0 && _ascend.month != 0 && _ascend.year != 0) {
            findViewById<EditText>(R.id.dateEditText).setText(
                    "${_ascend.day}.${_ascend.month}.${_ascend.year}")
        }

        findViewById<EditText>(R.id.notesEditText).setText(_ascend.notes)

        val partnerNames = _db.getPartnerNames(_ascend.partnerIds.orEmpty().toList())
        findViewById<EditText>(R.id.partnersEditText).setText(TextUtils.join(", ", partnerNames))
    }
}
