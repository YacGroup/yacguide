package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Rock {

    public static final String typeSummit = "G";
    public static final String typeMassif = "M";
    public static final String typeBoulder = "B";
    public static final String typeCave = "H";

    public static final char statusTemporarilyProhibited = 'Z';
    public static final char statusProhibited = 'X';
    public static final char statusUnofficial = 'N';

    @PrimaryKey
    private int id;

    private float nr;
    private String type;
    private char status; // 'X' = prohibited, 'Z' = temporarily prohibited, 'N' = unofficial, 'T' = partly prohibited
    private String name;
    private float longitude;
    private float latitude;
    private boolean ascended;
    private int parentId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getNr() {
        return nr;
    }

    public void setNr(float nr) {
        this.nr = nr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public boolean getAscended() {
        return ascended;
    }

    public void setAscended(boolean ascended) {
        this.ascended = ascended;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
}
