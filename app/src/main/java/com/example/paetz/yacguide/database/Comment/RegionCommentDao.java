package com.example.paetz.yacguide.database.Comment;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface RegionCommentDao {
    @Query("SELECT * FROM RegionComment WHERE regionId = :regionId")
    RegionComment[] getAll(int regionId);

    @Query("SELECT COUNT(*) FROM RegionComment WHERE regionId = :regionId")
    int getCommentCount(int regionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RegionComment comment);

    @Query("DELETE FROM RegionComment WHERE regionId = :regionId")
    void deleteAll(int regionId);
}
