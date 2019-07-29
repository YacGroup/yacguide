package com.example.paetz.yacguide.utils

import android.graphics.Color

import java.util.ArrayList
import java.util.HashMap
import java.util.TreeSet

enum class AscendStyle (val id: Int, val styleName: String, val color: Int) {

    // This needs to be in sync with sandsteinklettern.de!
    eSOLO(1, "Solo", Color.GREEN),
    eONSIGHT(2, "Onsight", Color.GREEN),
    eREDPOINT(3, "Rotpunkt", Color.GREEN),
    eALLFREE(4, "Alles frei", Color.GREEN),
    eHOCHGESCHLEUDERT(5, "Irgendwie hochgeschleudert", Color.GREEN),
    eALTERNATINGLEADS(6, "Wechselführung", Color.GREEN),
    eFOLLOWED(7, "Nachstieg", Color.GREEN),
    eHINTERHERGEHAMPELT(8, "Hinterhergehampelt", Color.GREEN),
    eBOTCHED(9, "Gesackt", Color.RED),
    eSEEN(10, "Zugesehen", Color.CYAN),
    eVISITED(11, "An den Einstieg gepinktelt", Color.CYAN),
    eHEARD(12, "Von gehört", Color.CYAN),
    ePROJECT(13, "Will ich klettern", Color.YELLOW);

    companion object {

        private val _BY_ID = HashMap<Int, AscendStyle>()
        private val _BY_NAME = HashMap<String, AscendStyle>()
        private val _BY_COLOR = HashMap<Int, AscendStyle>()

        init {
            for (style in values()) {
                _BY_ID[style.id] = style
                _BY_NAME[style.styleName] = style
                _BY_COLOR[style.color] = style
            }
        }

        fun fromName(name: String): AscendStyle? {
            return _BY_NAME[name]
        }

        fun fromId(id: Int): AscendStyle? {
            return _BY_ID[id]
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

        fun getPreferredColor(colors: Set<Int>): Int {
            return when {
                colors.contains(Color.GREEN) -> Color.GREEN
                colors.contains(Color.RED) -> Color.RED
                colors.contains(Color.YELLOW) -> Color.YELLOW
                colors.contains(Color.CYAN) -> Color.CYAN
                else -> Color.WHITE
            }
        }
    }
}