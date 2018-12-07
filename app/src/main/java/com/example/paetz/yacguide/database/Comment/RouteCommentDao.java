package com.example.paetz.yacguide.database.Comment;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface RouteCommentDao {
    @Query("SELECT * FROM RouteComment WHERE routeId = :routeId")
    RouteComment[] getAll(int routeId);

    @Query("SELECT COUNT(*) FROM RouteComment WHERE routeId = :routeId")
    int getCommentCount(int routeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RouteComment comment);

    @Query("DELETE FROM RouteComment WHERE routeId = :routeId")
    void deleteAll(int routeId);
}
