package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Route {

    @PrimaryKey
    private int id;

    private float nr;
    private String name;
    private String grade;
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
