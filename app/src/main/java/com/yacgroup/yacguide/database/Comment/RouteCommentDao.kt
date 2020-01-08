package com.yacgroup.yacguide.database.Comment

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface RouteCommentDao {
    @Query("SELECT * FROM RouteComment WHERE routeId = :routeId")
    fun getAll(routeId: Int): Array<RouteComment>

    @Query("SELECT COUNT(*) FROM RouteComment WHERE routeId = :routeId")
    fun getCommentCount(routeId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comment: RouteComment)

    @Query("DELETE FROM RouteComment WHERE routeId = :routeId")
    fun deleteAll(routeId: Int)
}
