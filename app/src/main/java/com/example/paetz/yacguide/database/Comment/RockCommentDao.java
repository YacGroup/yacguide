package com.example.paetz.yacguide.database.Comment;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface RockCommentDao {
    @Query("SELECT * FROM RockComment WHERE rockId = :rockId")
    RockComment[] getAll(int rockId);

    @Query("SELECT COUNT(*) FROM RockComment WHERE rockId = :rockId")
    int getCommentCount(int rockId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RockComment comment);

    @Query("DELETE FROM RockComment WHERE rockId = :rockId")
    void deleteAll(int rockId);
}
