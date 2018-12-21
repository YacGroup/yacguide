package com.example.paetz.yacguide.database.Comment;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

@Entity
public class RegionComment {

    // This needs to be in sync with sandsteinklettern.de!
    public static final Map<Integer, String> QUALITY_MAP = new HashMap<Integer, String>() {{
        put(1, "international bedeutend");
        put(2, "national bedeutend");
        put(3, "lohnt einen Abstecher");
        put(4, "lokal bedeutend");
        put(5, "unbedeutend");
    }};

    @PrimaryKey
    private int id;

    private int qualityId;
    private String text;
    private int regionId;

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

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }
}
