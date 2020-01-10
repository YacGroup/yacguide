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

        fun actionOnAscend(styleId: Int, objectId: Int, actionLead: (Int) -> Unit, actionFollow: (Int) -> Unit, actionBotch: (Int) -> Unit, actionProject: (Int) -> Unit) {
            when (styleId) {
                in 1..5 -> actionLead(objectId)
                in 6..8 -> actionFollow(objectId)
                9 -> actionBotch(objectId)
                13 -> actionProject(objectId)
            }
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