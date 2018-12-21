package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Rock {

    // This needs to be in sync with sandsteinklettern.de!
    public static final char typeSummit = 'G';
    public static final char typeMassif = 'M';
    public static final char typeBoulder = 'B';
    public static final char typeStonePit = 'S';
    public static final char typeAlpine = 'A';
    public static final char typeCave = 'H';
    public static final char typeUnofficial = 'N';

    public static final char statusTemporarilyProhibited = 'Z';
    public static final char statusProhibited = 'X';
    public static final char statusPartlyProhibited = 'T';

    @PrimaryKey
    private int id;

    private float nr;
    private char type;   // 'G' = Rock, 'M' = Massif, 'H' = Cage, 'B' = Boulder, 'N' = unofficial
    private char status; // 'X' = prohibited, 'Z' = temporarily prohibited, 'T' = partly prohibited
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

    public char getType() {
        return type;
    }

    public void setType(char type) {
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
