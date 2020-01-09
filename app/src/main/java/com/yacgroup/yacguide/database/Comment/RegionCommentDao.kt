package com.yacgroup.yacguide.database.Comment

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface RegionCommentDao {
    @Query("SELECT * FROM RegionComment WHERE regionId = :regionId")
    fun getAll(regionId: Int): Array<RegionComment>

    @Query("SELECT COUNT(*) FROM RegionComment WHERE regionId = :regionId")
    fun getCommentCount(regionId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comment: RegionComment)

    @Query("DELETE FROM RegionComment WHERE regionId = :regionId")
    fun deleteAll(regionId: Int)
}
