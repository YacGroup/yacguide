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

package com.yacgroup.yacguide.utils

import java.util.ArrayList
import java.util.HashMap
import java.util.TreeSet

enum class AscendStyle (val id: Int, val styleName: String) {

    // This needs to be in sync with sandsteinklettern.de!
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
    eVISITED(11, "An den Einstieg gepinktelt"),
    eHEARD(12, "Von gehört"),
    ePROJECT(13, "Will ich klettern");

    companion object {

        private val _BY_ID = HashMap<Int, AscendStyle>()
        private val _BY_NAME = HashMap<String, AscendStyle>()

        init {
            for (style in values()) {
                _BY_ID[style.id] = style
                _BY_NAME[style.styleName] = style
            }
        }

        fun fromName(name: String): AscendStyle? {
            return _BY_NAME[name]
        }

        fun fromId(id: Int): AscendStyle? {
            return _BY_ID[id]
        }

        fun bitMask(style: AscendStyle): Int {
            return bitMask(style.id)
        }

        fun bitMask(styleId: Int): Int {
            return 0b1 shl (styleId - 1)
        }

        fun isLead(mask: Int): Boolean {
            return mask and (bitMask(eSOLO)
                          or bitMask(eONSIGHT)
                          or bitMask(eREDPOINT)
                          or bitMask(eALLFREE)
                          or bitMask(eHOCHGESCHLEUDERT)) > 0
        }

        fun isFollow(mask: Int): Boolean {
            return mask and (bitMask(eALTERNATINGLEADS)
                          or bitMask(eFOLLOWED)
                          or bitMask(eHINTERHERGEHAMPELT)) > 0
        }

        fun isBotch(mask: Int): Boolean {
            return mask and bitMask(eBOTCHED) > 0
        }

        fun isWatching(mask: Int): Boolean {
            return mask and (bitMask(eSEEN)
                          or bitMask(eVISITED)
                          or bitMask(eHEARD)) > 0
        }

        fun isProject(mask: Int): Boolean {
            return mask and bitMask(ePROJECT) > 0
        }

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