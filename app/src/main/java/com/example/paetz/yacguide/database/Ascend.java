package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Ascend {

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
