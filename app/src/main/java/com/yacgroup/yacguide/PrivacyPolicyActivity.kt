/*
 * Copyright (C) 2020 Christian Sommer,
 *               2022 Axel Paetzold
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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.yacgroup.yacguide.databinding.ActivityPrivacyPolicyBinding
import com.yacgroup.yacguide.markwon.PrivacyPolicyMarkwonPlugin
import io.noties.markwon.Markwon

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.privacy_policy)
        _binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        _displayContent()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun _displayContent() {
        val markwon = Markwon.builder(this)
                .usePlugin(PrivacyPolicyMarkwonPlugin(getString(R.string.privacy_policy)))
                .build()
        val rawResource = resources.openRawResource(R.raw.privacy_policy)
        val privacyStr = rawResource.bufferedReader().use  { it.readText() }
        val privacyTextView = _binding.privacyPolicyTextView
        markwon.setMarkdown(privacyTextView, privacyStr)
    }
}
