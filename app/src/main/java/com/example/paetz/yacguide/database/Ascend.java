package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.example.paetz.yacguide.utils.AscendTypes;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Ascend {

    // This needs to be in sync with sandsteinklettern.de!
    public final static BiMap<Integer, String> CLIMBING_STYLES;

    static {
        final Map<Integer, String> styles = new HashMap<Integer, String>();
        styles.put(AscendTypes.SOLO, "Solo");
        styles.put(AscendTypes.ONSIGHT, "Onsight");
        styles.put(AscendTypes.REDPOINT, "Rotpunkt");
        styles.put(AscendTypes.ALLFREE, "Alles frei");
        styles.put(AscendTypes.HOCHGESCHLEUDERT, "Irgendwie hochgeschleudert");
        styles.put(AscendTypes.ALTERNATING_LEADS, "Wechselführung");
        styles.put(AscendTypes.FOLLOW, "Nachstieg");
        styles.put(AscendTypes.HINTERHERGEHAMPELT, "Hinterhergehampelt");
        styles.put(AscendTypes.BAILED, "Gesackt");
        styles.put(AscendTypes.SEEN, "Zugesehen");
        styles.put(AscendTypes.VISITED, "An den Einstieg gepinkelt");
        styles.put(AscendTypes.HEARD_ABOUT_IT, "Von gehört");
        styles.put(AscendTypes.PROJECT, "Will ich klettern");
        CLIMBING_STYLES = ImmutableBiMap.copyOf(Collections.unmodifiableMap(styles));
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int routeId;
    private int styleId; // id for os, RP, af, ...
    private int year;
    private int month;
    private int day;
    private ArrayList<Integer> partnerIds;
    private String notes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public int getStyleId() {
        return styleId;
    }

    public void setStyleId(int styleId) {
        this.styleId = styleId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public ArrayList<Integer> getPartnerIds() {
        return partnerIds;
    }

    public void setPartnerIds(ArrayList<Integer> partnerIds) {
        this.partnerIds = partnerIds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
