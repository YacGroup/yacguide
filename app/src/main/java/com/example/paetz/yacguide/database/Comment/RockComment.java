package com.example.paetz.yacguide.database.Comment;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

@Entity
public class RockComment {

    public final static Map<Integer, String> QUALITY_MAP = new HashMap<Integer, String>() {{
        put(1, "Hauptgipfel");
        put(2, "lohnender Gipfel");
        put(3, "Durchschnittsgipfel");
        put(4, "Quacke");
        put(5, "Dreckhaufen");
    }};

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
