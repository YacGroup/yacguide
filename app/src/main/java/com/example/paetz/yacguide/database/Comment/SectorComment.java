package com.example.paetz.yacguide.database.Comment;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

@Entity
public class SectorComment {

    // This needs to be in sync with sandsteinklettern.de!
    public final static Map<Integer, String> QUALITY_MAP = new HashMap<Integer, String>() {{
        put(1, "Hauptteilgebiet");
        put(2, "lohnendes Gebiet");
        put(3, "kann man mal hingehen");
        put(4, "weniger bedeutend");
        put(5, "unbedeutend");
    }};

    @PrimaryKey
    private int id;

    private int qualityId;
    private String text;
    private int sectorId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQualityId() {
        return qualityId;
    }

    public void setQualityId(int qualityId) {
        this.qualityId = qualityId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getSectorId() {
        return sectorId;
    }

    public void setSectorId(int sectorId) {
        this.sectorId = sectorId;
    }
}
