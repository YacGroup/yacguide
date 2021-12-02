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
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.yacgroup.yacguide.R

class SearchBarHandler(searchBarLayout: ConstraintLayout,
                       searchHintResource: Int,
                       checkBoxTitle: String,
                       checkBoxDefaultValue: Boolean,
                       private val _settings: SharedPreferences,
                       private val _updateCallback: (String, Boolean) -> Unit) {

    private var _namePart: String = ""
    private var _onlyOfficialObjects: Boolean = false

    init {
        _onlyOfficialObjects = _settings.getBoolean(
            checkBoxTitle,
            checkBoxDefaultValue)
        searchBarLayout.findViewById<CheckBox>(R.id.onlyOfficialObjectsCheckbox).apply {
            text = checkBoxTitle
            isChecked = _onlyOfficialObjects
            setOnClickListener(View.OnClickListener {
                _onlyOfficialObjects = isChecked
                _updateCallback(_namePart, _onlyOfficialObjects)
            })
        }

        searchBarLayout.findViewById<EditText>(R.id.searchEditText).apply {
            setHint(searchHintResource)
            onFocusChangeListener = View.OnFocusChangeListener { view, _ ->
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    _namePart = text.toString()
                    _updateCallback(_namePart, _onlyOfficialObjects)
                }
            })
        }

        _updateCallback(_namePart, _onlyOfficialObjects)
    }

    fun storeCustomSettings(key: String) {
        val editor = _settings.edit()
        editor.putBoolean(key, _onlyOfficialObjects)
        editor.commit()
    }
}