package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface CommentDao {
    @Query("SELECT * FROM Comment WHERE parentId = :parentId")
    Comment[] getAll(int parentId);

    @Query("SELECT COUNT(*) FROM Comment WHERE parentId = :parentId")
    int getCommentCount(int parentId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Comment comment);

    @Query("DELETE FROM Comment WHERE parentId = :parentId")
    void deleteAll(int parentId);
}
