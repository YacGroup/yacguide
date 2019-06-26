package com.example.paetz.yacguide.utils;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public enum AscendStyle {

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

    private static final Map<Integer, AscendStyle> _BY_ID = new HashMap<>();
    private static final Map<String, AscendStyle> _BY_NAME = new HashMap<>();
    private static final Map<Integer, AscendStyle> _BY_COLOR = new HashMap<>();

    static {
        for (AscendStyle style : values()) {
            _BY_ID.put(style.id, style);
            _BY_NAME.put(style.name, style);
            _BY_COLOR.put(style.color, style);
        }
    }

    public final int id;
    public final String name;
    public final int color;

    private AscendStyle(int id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public static AscendStyle fromName(String name) {
        return _BY_NAME.get(name);
    }

    public static AscendStyle fromId(int id) {
        return _BY_ID.get(id);
    }

    public static String[] getNames() {
        // We need to preserve order given be IDs
        final Set<Integer> ids = new TreeSet(_BY_ID.keySet());
        List<String> orderedNames = new ArrayList<>();
        for (final Integer id : ids) {
            orderedNames.add(fromId(id).name);
        }
        return orderedNames.toArray(new String[0]);
    }

    public static int getPreferredColor(Set<Integer> colors) {
        if (colors.contains(Color.GREEN)) {
            return Color.GREEN;
        } else if (colors.contains(Color.RED)) {
            return Color.RED;
        } else if (colors.contains(Color.YELLOW)) {
            return Color.YELLOW;
        } else if (colors.contains(Color.CYAN)) {
            return Color.CYAN;
        } else {
            return Color.WHITE;
        }
    }
}