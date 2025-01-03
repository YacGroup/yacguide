/*
 * Copyright (C) 2019, 2022, 2025 Axel Paetzold
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

import java.util.ArrayList
import java.util.HashMap
import java.util.TreeSet

enum class AscendStyle (val id: Int, val styleName: String) {

    // This needs to be in sync with sandsteinklettern.de!
    eUNKNOWN(0, "Unbekannt"),
    eSOLO(1, "Solo"),
    eONSIGHT(2, "Onsight"),
    eREDPOINT(3, "Rotpunkt"),
    eALLFREE(4, "Alles frei"),
    eHOCHGESCHLEUDERT(5, "Irgendwie hochgeschleudert"),
    eALTERNATINGLEADS(6, "Wechselführung"),
    eFOLLOWED(7, "Nachstieg"),
    eHINTERHERGEHAMPELT(8, "Hinterhergehampelt"),
    eBOTCHED(9, "Gesackt"),
    eSEEN(10, "Zugesehen"),
    eVISITED(11, "An den Einstieg gepinkelt"),
    eHEARD(12, "Von gehört"),
    ePROJECT(13, "Will ich klettern");

    companion object {

        private val _LEAD_BIT_MASK = (_bitMask(eSOLO)
                                   or _bitMask(eONSIGHT)
                                   or _bitMask(eREDPOINT)
                                   or _bitMask(eALLFREE)
                                   or _bitMask(eHOCHGESCHLEUDERT))
        private val _FOLLOW_BIT_MASK = (_bitMask(eUNKNOWN)
                                     or _bitMask(eALTERNATINGLEADS)
                                     or _bitMask(eFOLLOWED)
                                     or _bitMask(eHINTERHERGEHAMPELT))
        private val _BOTCH_BIT_MASK = _bitMask(eBOTCHED)
        private val _WATCHING_BIT_MASK = (_bitMask(eSEEN)
                                       or _bitMask(eVISITED)
                                       or _bitMask(eHEARD))
        private val _PROJECT_BIT_MASK = _bitMask(ePROJECT)

        private val _BY_ID = HashMap<Int, AscendStyle>()
        private val _BY_NAME = HashMap<String, AscendStyle>()

        init {
            for (style in entries) {
                _BY_ID[style.id] = style
                _BY_NAME[style.styleName] = style
            }
        }

        fun fromName(name: String) = _BY_NAME[name]
        fun fromId(id: Int) = _BY_ID[id]

        fun bitMask(styleId: Int) = 0b1 shl (styleId - 1)
        fun isLead(mask: Int) = _hasAscendBit(mask and _LEAD_BIT_MASK)
        fun isFollow(mask: Int) = _hasAscendBit(mask and _FOLLOW_BIT_MASK)
        fun isBotch(mask: Int) = _hasAscendBit(mask and _BOTCH_BIT_MASK)
        fun isWatching(mask: Int) = _hasAscendBit(mask and _WATCHING_BIT_MASK)
        fun isProject(mask: Int) = _hasAscendBit(mask and _PROJECT_BIT_MASK)

        fun deriveAscentColor(ascentBitMask: Int, leadColor: Int, followColor: Int, defaultColor: Int) = when {
            isLead(ascentBitMask) -> leadColor
            isFollow(ascentBitMask) -> followColor
            else -> defaultColor
        }

        fun deriveAscentDecoration(ascendsBitMask: Int, botchIcon: String, projectIcon: String, watchingIcon:String): String {
            val botchAdd = if (isBotch(ascendsBitMask)) botchIcon else ""
            val projectAdd = if (isProject(ascendsBitMask)) projectIcon else ""
            val watchingAdd = if (isWatching(ascendsBitMask)) watchingIcon else ""
            return "$botchAdd$projectAdd$watchingAdd"
        }

        private fun _bitMask(style: AscendStyle) = bitMask(style.id)

        private fun _hasAscendBit(mask: Int) = mask != 0

        // We need to preserve order given by IDs
        val names: Array<String>
            get() {
                val ids = TreeSet(_BY_ID.keys)
                val orderedNames = ArrayList<String>()
                for (id in ids) {
                    orderedNames.add(fromId(id)!!.styleName)
                }
                return orderedNames.toTypedArray()
            }
    }
}