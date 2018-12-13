package com.example.paetz.yacguide.database.Comment;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

@Entity
public class RouteComment {

    public final static Map<Integer, String> QUALITY_MAP = new HashMap<Integer, String>() {{
        put(1, "sehr lohnend");
        put(2, "lohnend");
        put(3, "ok");
        put(4, "Geschmackssache");
        put(5, "Müll");
    }};

    public final static Map<Integer, String> GRADE_MAP = new HashMap<Integer, String>() {{
        put(1, "I");
        put(2, "II");
        put(3, "III");
        put(4, "IV");
        put(5, "V");
        put(6, "VI");
        put(7, "VIIa");
        put(8, "VIIb");
        put(9, "VIIc");
        put(10, "VIIIa");
        put(11, "VIIIb");
        put(12, "VIIIc");
        put(13, "IXa");
        put(14, "IXb");
        put(15, "IXc");
        put(16, "Xa");
        put(17, "Xb");
        put(18, "Xc");
        put(19, "XIa");
        put(20, "XIb");
        put(21, "XIc");
        put(22, "XIIa");
        put(23, "XIIb");
        put(24, "XIIc");
    }};

    public final static Map<Integer, String> SECURITY_MAP = new HashMap<Integer, String>() {{
        put(1, "übersichert");
        put(2, "gut");
        put(3, "ausreichend");
        put(4, "kompliziert");
        put(5, "ungenügend");
        put(6, "kamikaze");
    }};

    public final static Map<Integer, String> WETNESS_MAP = new HashMap<Integer, String>() {{
        put(1, "Regenweg");
        put(2, "schnellabtrocknend");
        put(3, "normal abtrocknend");
        put(4, "oft feucht");
        put(5, "immer nass");
    }};

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
