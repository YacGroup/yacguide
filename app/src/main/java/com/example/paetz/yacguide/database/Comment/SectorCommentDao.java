package com.example.paetz.yacguide.database.Comment;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface SectorCommentDao {
    @Query("SELECT * FROM SectorComment WHERE sectorId = :sectorId")
    SectorComment[] getAll(int sectorId);

    @Query("SELECT COUNT(*) FROM SectorComment WHERE sectorId = :sectorId")
    int getCommentCount(int sectorId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SectorComment comment);

    @Query("DELETE FROM SectorComment WHERE sectorId = :sectorId")
    void deleteAll(int sectorId);
}
