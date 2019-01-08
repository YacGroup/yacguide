package com.example.paetz.yacguide.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface PartnerDao {
    @Query("SELECT * FROM Partner")
    Partner[] getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Partner partner);

    @Query("SELECT Id FROM Partner WHERE Name = :name")
    int getId(String name);

    @Query("SELECT * FROM Partner WHERE Id = :id")
    Partner getPartner(int id);

    @Delete
    void delete(Partner partner);

    @Query("DELETE FROM Partner")
    void deleteAll();
}
