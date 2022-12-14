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

package com.yacgroup.yacguide

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.network.CountryAndRegionParser
import com.yacgroup.yacguide.utils.IntentConstants

const val TIME_OUT_MS = 1500L

class LaunchActivity : AppCompatActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _customSettings: SharedPreferences
    private lateinit var _updateHandler: UpdateHandler

    inner class Timer : CountDownTimer(TIME_OUT_MS, TIME_OUT_MS) {
        override fun onTick(msUntilFinish: Long) {}
        override fun onFinish() {
            _updateHandler.abort()
            _switchToDatabase()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        _db = DatabaseWrapper(this)
        _customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)

        _updateHandler = UpdateHandler(this, CountryAndRegionParser(_db))
        _updateHandler.update(isRecurring = false, isSilent = true)
        Timer().start()
    }

    private fun _switchToDatabase() {
        val defaultRegionId = _customSettings.getInt(getString(R.string.default_region_key), R.integer.unknown_id)
        val intent = if (defaultRegionId == R.integer.unknown_id)
            Intent(this, CountryActivity::class.java)
        else
            Intent(this, SectorActivity::class.java).apply {
                putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eSector.value)
                putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, defaultRegionId)
                putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, _db.getRegion(defaultRegionId)?.name.orEmpty())
            }
        startActivity(intent)
        finish()
    }
}
