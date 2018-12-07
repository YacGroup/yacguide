package com.example.paetz.yacguide.database.Comment;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Entity
public class RockComment {

    public final static BiMap<Integer, String> QUALITY_MAP;
    static {
        final Map<Integer, String> types = new HashMap<Integer, String>();
        types.put(1, "Hauptgipfel");
        types.put(2, "lohnender Gipfel");
        types.put(3, "Durchschnittsgipfel");
        types.put(4, "Quacke");
        types.put(5, "Dreckhaufen");
        QUALITY_MAP = ImmutableBiMap.copyOf(Collections.unmodifiableMap(types));
    }

    @PrimaryKey
    private int id;

    private int qualityId;
    private String text;
    private int rockId;

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

    public int getRockId() {
        return rockId;
    }

    public void setRockId(int rockId) {
        this.rockId = rockId;
    }
}
