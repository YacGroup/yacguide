package com.example.paetz.yacguide.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface CountryDao {

    @get:Query("SELECT * FROM Country")
    val all: Array<Country>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(country: Country)

    @Query("DELETE FROM Country")
    fun deleteAll()
}
