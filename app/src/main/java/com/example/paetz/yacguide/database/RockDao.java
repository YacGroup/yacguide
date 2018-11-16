package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface RockDao {
    @Query("SELECT * FROM Rock WHERE parentId = :parentId ORDER BY nr")
    Rock[] getAll(int parentId);

    @Query("SELECT * FROM Rock WHERE id = :id")
    Rock getRock(int id);

    @Query("UPDATE Rock SET ascended = :ascended WHERE id = :id")
    void updateAscended(boolean ascended, int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Rock rock);

    @Query("DELETE FROM Rock WHERE parentId = :parentId")
    void deleteAll(int parentId);
}
