package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

@Entity
public class Route {

    // This needs to be in sync with sandsteinklettern.de!
    public static final Map<Integer, String> STATUS = new HashMap<Integer, String>() {{
        put(1, "anerkannt.");
        put(2, "zeitlich gesperrt.");
        put(3, "voll gesperrt.");
        put(4, "eine Erstbegehung.");
        put(5, "nicht anerkannt.");
        put(6, "nur eine Erwähnung.");
        put(7, "ein Projekt.");
    }};

    @PrimaryKey
    private int id;

    private float nr;
    private int statusId;
    private String name;
    private String grade;
    private String firstAscendLeader;
    private String firstAscendFollower;
    private String firstAscendDate;
    private String typeOfClimbing;
    private String description;
    private int ascendCount;
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

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getFirstAscendLeader() {
        return firstAscendLeader;
    }

    public void setFirstAscendLeader(String firstAscendLeader) {
        this.firstAscendLeader = firstAscendLeader;
    }

    public String getFirstAscendFollower() {
        return firstAscendFollower;
    }

    public void setFirstAscendFollower(String firstAscendFollower) {
        this.firstAscendFollower = firstAscendFollower;
    }

    public String getFirstAscendDate() {
        return firstAscendDate;
    }

    public void setFirstAscendDate(String firstAscendDate) {
        this.firstAscendDate = firstAscendDate;
    }

    public String getTypeOfClimbing() {
        return typeOfClimbing;
    }

    public void setTypeOfClimbing(String typeOfClimbing) {
        this.typeOfClimbing = typeOfClimbing;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAscendCount() {
        return ascendCount;
    }

    public void setAscendCount(int ascendCount) {
        this.ascendCount = ascendCount;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
}
