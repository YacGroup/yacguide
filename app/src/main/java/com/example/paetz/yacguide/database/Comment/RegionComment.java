package com.example.paetz.yacguide.database.Comment;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Entity
public class RegionComment {

    public final static BiMap<Integer, String> QUALITY_MAP;
    static {
        final Map<Integer, String> types = new HashMap<Integer, String>();
        types.put(1, "international bedeutend");
        types.put(2, "national bedeutend");
        types.put(3, "lohnt einen Abstecher");
        types.put(4, "lokal bedeutend");
        types.put(5, "unbedeutend");
        QUALITY_MAP = ImmutableBiMap.copyOf(Collections.unmodifiableMap(types));
    }

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
