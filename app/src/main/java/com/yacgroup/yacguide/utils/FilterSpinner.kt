/*
 * Copyright (C) 2022 Axel Paetzold
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

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDialog
import com.yacgroup.yacguide.R

class FilterSpinner(
    private val _searchDialog: AppCompatDialog,
    private val _spinner: Spinner,
    private val _spinnerValuesMap: LinkedHashMap<Int, String>,
    private val _spinnerListener: FilterSpinnerListener
) {

    fun create() {
        val adapter = ArrayAdapter<CharSequence>(
            _searchDialog.context,
            R.layout.spinner_item,
            _spinnerValuesMap.values.toTypedArray()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        _spinner.adapter = adapter
        _spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectionName = parent.getItemAtPosition(position).toString()
                val selectionKey = _spinnerValuesMap.keys.first { _spinnerValuesMap[it] == selectionName }
                _spinnerListener.onFilterSelected(_spinner.id, selectionKey)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
