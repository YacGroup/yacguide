package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface CountryDao {

    @Query("SELECT * FROM Country")
    Country[] getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Country country);

    @Query("DELETE FROM Country")
    void deleteAll();
}
