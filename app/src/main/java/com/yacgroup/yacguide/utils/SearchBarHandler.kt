/*
 * Copyright (C) 2021, 2023 Axel Paetzold
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
import com.google.android.material.textfield.TextInputLayout
import com.yacgroup.yacguide.R

class SearchBarHandler(searchBarLayout: ConstraintLayout,
                       searchHintResource: Int,
                       private val _checkBoxTitle: String = "",
                       checkBoxDefaultValue: Boolean = false,
                       private val _settings: SharedPreferences? = null,
                       private val _settingsKey: String,
                       initCallback: (Boolean) -> Unit = {},
                       updateCallback: (String, Boolean) -> Unit = { _, _ ->}) {

    private var _namePart: String = ""
    private var _checkBoxIsChecked: Boolean = false

    init {
        _checkBoxIsChecked = _settings?.getBoolean(
            _settingsKey,
            checkBoxDefaultValue
        ) ?: false
        searchBarLayout.findViewById<CheckBox>(R.id.filterCheckbox).apply {
            text = _checkBoxTitle
            isChecked = _checkBoxIsChecked
            setOnClickListener {
                _checkBoxIsChecked = isChecked
                updateCallback(_namePart, _checkBoxIsChecked)
            }
        }

        searchBarLayout.findViewById<TextInputLayout>(R.id.searchTextInputLayout).apply {
            setHint(searchHintResource)
        }
        searchBarLayout.findViewById<EditText>(R.id.searchEditText).apply {
            onFocusChangeListener = View.OnFocusChangeListener { view, _ ->
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    _namePart = text.toString()
                    updateCallback(_namePart, _checkBoxIsChecked)
                }
            })
        }

        initCallback(_checkBoxIsChecked)
    }

    fun storeCustomSettings() {
        _settings?.edit()?.apply {
            putBoolean(_settingsKey, _checkBoxIsChecked)
            apply()
        }
    }
}
