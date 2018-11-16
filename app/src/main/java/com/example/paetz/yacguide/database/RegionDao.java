package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface RegionDao {
    @Query("SELECT * FROM Region WHERE country = :countryName")
    Region[] getAll(String countryName);

    @Query("SELECT * FROM Region WHERE id = :id")
    Region getRegion(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Region region);

    @Query("DELETE FROM Region WHERE Country = :countryName")
    void deleteAll(String countryName);
}
