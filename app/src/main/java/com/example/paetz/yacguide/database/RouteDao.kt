package com.example.paetz.yacguide.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface RouteDao {
    @Query("SELECT * FROM Route WHERE parentId = :parentId ORDER BY nr")
    fun getAll(parentId: Int): Array<Route>

    @Query("SELECT * FROM Route WHERE id = :id")
    fun getRoute(id: Int): Route

    @Query("SELECT ascendCount FROM Route WHERE id = :id")
    fun getAscendCount(id: Int): Int

    @Query("UPDATE Route SET description = :description WHERE id = :id")
    fun updateDescription(description: String, id: Int)

    @Query("UPDATE Route SET ascendCount = :ascendCount WHERE id = :id")
    fun updateAscendCount(ascendCount: Int, id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(route: Route)

    @Query("DELETE FROM Route WHERE parentId = :parentId")
    fun deleteAll(parentId: Int)
}
