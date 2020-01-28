package com.yacgroup.yacguide.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface RockDao {
    @Query("SELECT * FROM Rock WHERE parentId = :parentId ORDER BY nr")
    fun getAll(parentId: Int): Array<Rock>

    @Query("SELECT * FROM Rock ORDER BY nr")
    fun getAllRocks(): Array<Rock>

    @Query("SELECT * FROM Rock WHERE id = :id")
    fun getRock(id: Int): Rock?

    @Query("UPDATE Rock SET ascendCountLead = ascendCountLead + 1 WHERE id = :id")
    fun incAscendCountLead(id: Int)

    @Query("UPDATE Rock SET ascendCountFollow = ascendCountFollow + 1 WHERE id = :id")
    fun incAscendCountFollow(id: Int)

    @Query("UPDATE Rock SET ascendCountBotch = ascendCountBotch + 1 WHERE id = :id")
    fun incAscendCountBotch(id: Int)

    @Query("UPDATE Rock SET ascendCountWatching = ascendCountWatching + 1 WHERE id = :id")
    fun incAscendCountWatching(id: Int)

    @Query("UPDATE Rock SET ascendCountProject = ascendCountProject + 1 WHERE id = :id")
    fun incAscendCountProject(id: Int)

    @Query("UPDATE Rock SET ascendCountLead = ascendCountLead - 1 WHERE id = :id")
    fun decAscendCountLead(id: Int)

    @Query("UPDATE Rock SET ascendCountFollow = ascendCountFollow - 1 WHERE id = :id")
    fun decAscendCountFollow(id: Int)

    @Query("UPDATE Rock SET ascendCountBotch = ascendCountBotch - 1 WHERE id = :id")
    fun decAscendCountBotch(id: Int)

    @Query("UPDATE Rock SET ascendCountWatching = ascendCountWatching - 1 WHERE id = :id")
    fun decAscendCountWatching(id: Int)

    @Query("UPDATE Rock SET ascendCountProject = ascendCountProject - 1 WHERE id = :id")
    fun decAscendCountProject(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rock: Rock)

    @Query("DELETE FROM Rock WHERE parentId = :parentId")
    fun deleteAll(parentId: Int)
}
