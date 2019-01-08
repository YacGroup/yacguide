package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface AscendDao {
    @Query("SELECT * FROM Ascend")
    Ascend[] getAll();

    @Query("SELECT * FROM Ascend WHERE year = :year ORDER BY year, month, day")
    Ascend[] getAll(int year);

    @Query("SELECT * FROM Ascend WHERE routeId = :routeId ORDER BY year, month, day")
    Ascend[] getAscendsForRoute(int routeId);

    @Query("SELECT * FROM Ascend WHERE id = :id")
    Ascend getAscend(int id);

    @Query("SELECT DISTINCT year FROM Ascend")
    int[] getYears();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ascend ascend);

    @Delete
    void delete(Ascend ascend);

    @Query("DELETE FROM Ascend")
    void deleteAll();
}
