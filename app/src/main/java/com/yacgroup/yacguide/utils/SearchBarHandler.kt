/*
 * Copyright (C) 2021 Axel Paetzold
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

package com.yacgroup.yacguide.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import com.yacgroup.yacguide.ClimbingObjectFilter
import com.yacgroup.yacguide.R

class SearchBarHandler(searchBarLayout: ConstraintLayout,
                       searchHintResource: Int,
                       filterNamesResource: Int,
                       private val _updateCallback: (String, ClimbingObjectFilter) -> Unit) : AdapterView.OnItemSelectedListener {

    private val _filterMap = mapOf(
        0 to ClimbingObjectFilter.eNone,
        1 to ClimbingObjectFilter.eOfficial,
        2 to ClimbingObjectFilter.eProject,
        3 to ClimbingObjectFilter.eBotch
    )
    private var _filter: ClimbingObjectFilter = ClimbingObjectFilter.eNone
    private var _namePart: String = ""

    init {
        val filterSpinner: Spinner = searchBarLayout.findViewById(R.id.filterSpinner)
        ArrayAdapter.createFromResource(
                searchBarLayout.context,
                filterNamesResource,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            filterSpinner.adapter = adapter
            filterSpinner.onItemSelectedListener = this
        }

        val searchEditText = searchBarLayout.findViewById<EditText>(R.id.searchEditText)
        searchEditText.setHint(searchHintResource)
        searchEditText.onFocusChangeListener = View.OnFocusChangeListener { view, _ ->
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                _namePart = searchEditText.text.toString()
                _updateCallback(_namePart, _filter)
            }
        })
    }

    // AdapterView.OnItemSelectedListener
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        _filter = _filterMap[position] ?: ClimbingObjectFilter.eNone
        _updateCallback(_namePart, _filter)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

}