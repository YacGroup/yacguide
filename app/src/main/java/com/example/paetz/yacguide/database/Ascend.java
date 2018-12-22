package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

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
        styles.put(1, "Solo");
        styles.put(2, "Onsight");
        styles.put(3, "Rotpunkt");
        styles.put(4, "Alles frei");
        styles.put(5, "Irgendwie hochgeschleudert");
        styles.put(6, "Wechselführung");
        styles.put(7, "Nachstieg");
        styles.put(8, "Hinterhergehampelt");
        styles.put(9, "Gesackt");
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

    public Ascend() {}

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
