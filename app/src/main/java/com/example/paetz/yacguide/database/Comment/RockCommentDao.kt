package com.example.paetz.yacguide.database.Comment

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface RockCommentDao {
    @Query("SELECT * FROM RockComment WHERE rockId = :rockId")
    fun getAll(rockId: Int): Array<RockComment>

    @Query("SELECT COUNT(*) FROM RockComment WHERE rockId = :rockId")
    fun getCommentCount(rockId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comment: RockComment)

    @Query("DELETE FROM RockComment WHERE rockId = :rockId")
    fun deleteAll(rockId: Int)
}
