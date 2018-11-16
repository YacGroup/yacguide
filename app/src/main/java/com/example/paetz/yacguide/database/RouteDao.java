package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface RouteDao {
    @Query("SELECT * FROM Route WHERE parentId = :parentId ORDER BY nr")
    Route[] getAll(int parentId);

    @Query("SELECT * FROM Route WHERE id = :id")
    Route getRoute(int id);

    @Query("SELECT ascendCount FROM Route WHERE id = :id")
    int getAscendCount(int id);

    @Query("UPDATE Route SET description = :description WHERE id = :id")
    void updateDescription(String description, int id);

    @Query("UPDATE Route SET ascendCount = :ascendCount WHERE id = :id")
    void updateAscendCount(int ascendCount, int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Route route);

    @Query("DELETE FROM Route WHERE parentId = :parentId")
    void deleteAll(int parentId);
}
