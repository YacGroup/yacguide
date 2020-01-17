package com.yacgroup.yacguide.utils

import android.graphics.Color

class CustomSettings {

    private val _colorLead = Color.GREEN
    private val _colorFollow = Color.GREEN

    fun getColorLead(): Int {
        return _colorLead
    }

    fun getColorFollow(): Int {
        return _colorFollow
    }

    companion object {

        private var _instance: CustomSettings? = null

        fun getCustomSettings(): CustomSettings {
            if (_instance == null) {
                _instance = CustomSettings()
            }
            return _instance as CustomSettings
        }
    }
}
