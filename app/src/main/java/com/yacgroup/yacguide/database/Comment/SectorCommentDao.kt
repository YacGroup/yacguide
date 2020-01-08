package com.yacgroup.yacguide.database.Comment

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface SectorCommentDao {
    @Query("SELECT * FROM SectorComment WHERE sectorId = :sectorId")
    fun getAll(sectorId: Int): Array<SectorComment>

    @Query("SELECT COUNT(*) FROM SectorComment WHERE sectorId = :sectorId")
    fun getCommentCount(sectorId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comment: SectorComment)

    @Query("DELETE FROM SectorComment WHERE sectorId = :sectorId")
    fun deleteAll(sectorId: Int)
}
