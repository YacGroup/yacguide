package com.example.paetz.yacguide.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface PartnerDao {
    @get:Query("SELECT * FROM Partner")
    val all: Array<Partner>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(partner: Partner)

    @Query("SELECT Id FROM Partner WHERE Name = :name")
    fun getId(name: String): Int

    @Query("SELECT * FROM Partner WHERE Id = :id")
    fun getPartner(id: Int): Partner?

    @Delete
    fun delete(partner: Partner)

    @Query("DELETE FROM Partner")
    fun deleteAll()
}
