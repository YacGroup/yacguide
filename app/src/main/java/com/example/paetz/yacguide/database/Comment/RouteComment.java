package com.example.paetz.yacguide.database.Comment;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Entity
public class RouteComment {

    public final static BiMap<Integer, String> QUALITY_MAP;
    static {
        final Map<Integer, String> types = new HashMap<Integer, String>();
        types.put(1, "sehr lohnend");
        types.put(2, "lohnend");
        types.put(3, "ok");
        types.put(4, "Geschmackssache");
        types.put(5, "Müll");
        QUALITY_MAP = ImmutableBiMap.copyOf(Collections.unmodifiableMap(types));
    }

    public final static BiMap<Integer, String> GRADE_MAP;
    static {
        final Map<Integer, String> types = new HashMap<Integer, String>();
        types.put(1, "I");
        types.put(2, "II");
        types.put(3, "III");
        types.put(4, "IV");
        types.put(5, "V");
        types.put(6, "VI");
        types.put(7, "VIIa");
        types.put(8, "VIIb");
        types.put(9, "VIIc");
        types.put(10, "VIIIa");
        types.put(11, "VIIIb");
        types.put(12, "VIIIc");
        types.put(13, "IXa");
        types.put(14, "IXb");
        types.put(15, "IXc");
        types.put(16, "Xa");
        types.put(17, "Xb");
        types.put(18, "Xc");
        types.put(19, "XIa");
        types.put(20, "XIb");
        types.put(21, "XIc");
        types.put(22, "XIIa");
        types.put(23, "XIIb");
        types.put(24, "XIIc");
        GRADE_MAP = ImmutableBiMap.copyOf(Collections.unmodifiableMap(types));
    }

    public final static BiMap<Integer, String> SECURITY_MAP;
    static {
        final Map<Integer, String> types = new HashMap<Integer, String>();
        types.put(1, "übersichert");
        types.put(2, "gut");
        types.put(3, "ausreichend");
        types.put(4, "kompliziert");
        types.put(5, "ungenügend");
        types.put(6, "kamikaze");
        SECURITY_MAP = ImmutableBiMap.copyOf(Collections.unmodifiableMap(types));
    }

    public final static BiMap<Integer, String> WETNESS_MAP;
    static {
        final Map<Integer, String> types = new HashMap<Integer, String>();
        types.put(1, "Regenweg");
        types.put(2, "schnellabtrocknend");
        types.put(3, "normal abtrocknend");
        types.put(4, "oft feucht");
        types.put(5, "immer nass");
        WETNESS_MAP = ImmutableBiMap.copyOf(Collections.unmodifiableMap(types));
    }

    @PrimaryKey
    private int id;

    private int qualityId;
    private int securityId;
    private int wetnessId;
    private int gradeId;
    private String text;
    private int routeId;

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

    public int getSecurityId() {
        return securityId;
    }

    public void setSecurityId(int securityId) {
        this.securityId = securityId;
    }

    public int getWetnessId() {
        return wetnessId;
    }

    public void setWetnessId(int wetnessId) {
        this.wetnessId = wetnessId;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }
}
