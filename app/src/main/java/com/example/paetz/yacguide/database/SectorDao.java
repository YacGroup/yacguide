package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface SectorDao {
    @Query("SELECT * FROM Sector WHERE parentId = :parentId ORDER BY nr")
    Sector[] getAll(int parentId);

    @Query("SELECT * FROM Sector WHERE id = :id")
    Sector getSector(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Sector sector);

    @Query("DELETE FROM Sector WHERE parentId = :parentId")
    void deleteAll(int parentId);
}
