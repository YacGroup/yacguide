/*
 * Copyright (C) 2026 Christian Sommer
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.yacgroup.yacguide.databinding.ProgressDialogBinding

/**
 * Progress spinner dialog
 *
 * @param _text Text shown below the spinner
 */
class ProgressDialog(private val _text: String = "") : DialogFragment() {

    private lateinit var _viewBinding: ProgressDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use a theme that removes the default dialog title/frame.
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = ProgressDialogBinding.inflate(inflater, container, false)
        return _viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _viewBinding.progressBarText.text = _text
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // Ensure that the dialog itself has no white box around the spinner.
            setBackgroundDrawableResource(android.R.color.transparent)
            // Gray-out the rest of the screen
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.5f)
            // Make dialog full screen so the dim covers everything
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        // Prevent closing the dialog when clicking outside
        isCancelable = false
    }
}
